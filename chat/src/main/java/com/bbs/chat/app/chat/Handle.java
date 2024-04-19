package com.bbs.chat.app.chat;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.bbs.api.Auth;
import com.bbs.chat.app.chat.api.OnlineCount;
import com.bbs.chat.app.chat.cache.ChatListCache;
import com.bbs.chat.app.chat.session.SessionManage;
import com.bbs.chat.app.chat.queue.UserMessageQueue;
import com.bbs.chat.app.chat.vo.Message;
import com.bbs.chat.app.chat.vo.UnreadMessage;
import com.bbs.chat.converter.MessageConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import javax.websocket.EncodeException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.bbs.chat.app.chat.ClientSocketCode.UNREAD_MESSAGE;
import static com.bbs.chat.app.chat.ClientSocketCode.UNREAD_MESSAGES;
import static com.bbs.chat.app.chat.Interceptor.SOCKET_ATTR_TOKEN;
import static com.bbs.chat.app.chat.Interceptor.SOCKET_ATTR_UID;
import static com.bbs.chat.enums.MessageType.TEXT;
import static java.util.Objects.nonNull;

@Slf4j
@Component
public class Handle extends TextWebSocketHandler implements MessageListener {

    @Resource
    private UserMessageQueue userMessageQueue;
    @Resource
    private Auth.UserAPI api;
    @Resource
    private MessageConverter converter;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ChatListCache chatListCache;

    public static final String TOPIC = "chat:user:message:sync";
    /**
     * socket 建立成功事件
     */
    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) {
        Long id = getLoginUID(session);
        if (nonNull(id)) {
            // 用户连接成功，放入在线用户缓存
            SessionManage.add(id, session);
            if(SessionManage.notExists(id)) OnlineCount.incr(); // 在线数 +1

            try {
                List<String> allUnreadMessage = userMessageQueue.getAllUnreadMessage(id);
                if(CollectionUtils.isNotEmpty(allUnreadMessage)) {
                    Set<Long> ids = new HashSet<>();
                    Map<Long, List<UserMessageQueue.Message>> messageMap = new HashMap<>();
                    for (String unreadMessageStr : allUnreadMessage) {
                        UserMessageQueue.Message message = JSONUtil.toBean(unreadMessageStr, UserMessageQueue.Message.class);
                        Long sendUserID = message.getSourceUID();

                        ids.add(sendUserID);

                        messageMap.putIfAbsent(sendUserID, new ArrayList<>());
                        List<UserMessageQueue.Message> messageList = messageMap.get(sendUserID);

                        messageList.add(message);
                    }
                    String loginUserToken = getLoginUserToken(session);
                    List<UnreadMessage> unreadMessages = api.getUserList(loginUserToken, new ArrayList<>(ids)).stream().map(user -> {
                        UnreadMessage message = converter.toUnreadMSG(user);
                        message.setUnreadMessage(messageMap.get(user.getId()));
                        return message;
                    }).collect(Collectors.toList());

                    Message message = new Message();
                    message.setCode(UNREAD_MESSAGES);
                    message.setSize(allUnreadMessage.size());
                    session.sendMessage(new TextMessage(JSONUtil.toJsonPrettyStr(unreadMessages)));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("用户登录已经失效!");
        }
    }

    /**
     * 接收消息事件
     */
    @Override
    public void handleTextMessage(@NotNull WebSocketSession session, TextMessage param) {
        // 获得客户端传来的消息
        String sendTime = DateUtil.now();
        UserMessageQueue.Message message = JSONUtil.toBean(param.getPayload(), UserMessageQueue.Message.class);
        Auth.UserAPI.User loginUser = api.getLoginUser(message.getToken());
        Long currentUID = loginUser.getId();
        Long targetUID = message.getTargetUID();
        message.setSourceUID(currentUID);
        message.setTime(sendTime);
        try {
            WebSocketSession targetSession = SessionManage.search(targetUID);
            // 目标用户是否连接到当前服务
            if(targetUserSocketIsExistsCurrentServer(targetSession)) {
                message.setToken(null);
                dirSend(targetSession, message);
            } else {
                saveToUnreadQueue(currentUID, message); //1. 将消息，保存到对方的【未读队列】中
                broadcastEvent(currentUID, targetUID, sendTime); //2. 广播：有人发消息了
            }
            chatListCache.newChat(currentUID, targetUID, message);   //双方聊天列表，新增对象，且目标用户增加未读消息
        } catch (IOException | EncodeException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * socket 断开连接时
     */
    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus status) {
        Long id = getLoginUID(session);
        if (nonNull(id)) {
            // 用户退出，移除缓存
            if(SessionManage.exists(id)){
                SessionManage.remove(id);
                if(SessionManage.isNotEmpty()) OnlineCount.decr();
                log.info("用户 {} 退出,当前在线人数为: {}", id, OnlineCount.count);
            }
        }
    }

    private Long getLoginUID(WebSocketSession session) {
        Object uid = session.getAttributes().get(SOCKET_ATTR_UID);
        return (Long) uid;
    }

    private String getLoginUserToken(WebSocketSession session) {
        Object token = session.getAttributes().get(SOCKET_ATTR_TOKEN);
        return (String) token;
    }

    private Boolean targetUserSocketIsExistsCurrentServer(WebSocketSession session) {
        return nonNull(session);
    }

    /**
     * 直接发送
     * @param session 消息目标的 Socket
     * @param message 消息
     * @throws EncodeException 消息编码异常
     * @throws IOException 消息发送异常
     */
    private void dirSend(WebSocketSession session, UserMessageQueue.Message message) throws EncodeException, IOException {
        session.sendMessage(new TextMessage(JSONUtil.toJsonPrettyStr(
                new Message(UNREAD_MESSAGES, JSONUtil.toJsonPrettyStr(message), NumberUtils.INTEGER_ONE)
        )));
    }

    /**
     * 保存消息到目标用户的未读消息队列
     * @param sourceUID 消息源用户 ID
     * @param message 消息
     */
    private void saveToUnreadQueue(Long sourceUID, UserMessageQueue.Message message) {
        userMessageQueue.sendMessageThrow(sourceUID, message);
    }

    private void broadcastEvent(Long sourceUID, Long targetUID, String time) {
        Event event = new Event(sourceUID, targetUID, TEXT, time);
        String eventJSONStr = JSONUtil.toJsonStr(event);
        log.debug("[MessageService::sendTextMessage] 广播事件 event={}", eventJSONStr);
        stringRedisTemplate.convertAndSend(TOPIC, eventJSONStr);
    }
    @Override
    public void onMessage(org.springframework.data.redis.connection.Message message, byte[] pattern) {
        Event event = JSONUtil.toBean(new String(message.getBody()), Event.class);
        try {
            WebSocketSession session = SessionManage.search(event.getTargetUID());
            log.debug("接收到 Redis 订阅消息: message={};", message);
            if(nonNull(session)) {
                String unreadMessage = userMessageQueue.getUnreadMessage(event.getTargetUID());
                log.debug("从 Redis 中获取用户未读消息: unreadMessage={}", unreadMessage);
                session.sendMessage(new TextMessage(
                        JSONUtil.toJsonPrettyStr(new Message(UNREAD_MESSAGE, unreadMessage, NumberUtils.INTEGER_ONE))
                ));
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
