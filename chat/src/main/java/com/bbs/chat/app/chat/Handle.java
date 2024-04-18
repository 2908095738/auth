package com.bbs.chat.app.chat;

import com.bbs.chat.app.chat.api.OnlineCount;
import com.bbs.chat.app.chat.session.SessionManage;
import com.bbs.chat.app.chat.queue.UserMessageQueue;
import com.bbs.chat.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;

import static com.bbs.chat.app.chat.Interceptor.SOCKET_ATTR_UID;
import static java.util.Objects.nonNull;

@Slf4j
@Component
public class Handle extends TextWebSocketHandler {

    @Resource
    private UserMessageQueue userMessageQueue;
    @Resource
    private MessageService messageService;

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
                Integer unreadMessageSize = unreadSize(id);
                session.sendMessage(new TextMessage(unreadMessageSize.toString()));
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
    protected void handleTextMessage(@NotNull WebSocketSession session, TextMessage message) {
        // 获得客户端传来的消息
        Long id = getLoginUID(session);
        messageService.sendTextMessage(id, message.toString());
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

    /**
     * 向用户发送消息（由 Redis 订阅发布调用）
     */
    public void sendMessageToUser(Long id, String message){
        try {
            WebSocketSession session = SessionManage.search(id);
            if(nonNull(session)) {
                session.sendMessage(new TextMessage(message));
            } else {
                messageService.sendTextMessage(id, message);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    private Integer unreadSize(Long userID) {
        return userMessageQueue.getAllUnreadMessageSize(userID);
    }
}
