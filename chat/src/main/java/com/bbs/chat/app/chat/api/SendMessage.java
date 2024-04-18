package com.bbs.chat.app.chat.api;

import cn.hutool.json.JSONUtil;
import com.bbs.api.Auth;
import com.bbs.chat.app.chat.queue.UserMessageQueue;
import com.bbs.chat.app.chat.session.SessionManage;
import com.bbs.chat.conf.RedisConf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;
import javax.websocket.EncodeException;
import java.io.IOException;
import java.util.Date;

import static com.bbs.chat.enums.MessageType.TEXT;
import static java.util.Objects.nonNull;

@Slf4j
@RestController
@RequestMapping
public class SendMessage {
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private UserMessageQueue queue;
    @Resource
    private Auth.UserAPI api;
    public static final String TOPIC = "chat:message:sync";

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Param {

        private String token;
        private String message;
    }

    @PutMapping("/socket/{id}")
    public void push(@PathVariable("id") Long targetUID, @RequestBody Param param) {
        Date time = new Date();
        try {
            WebSocketSession session = SessionManage.search(targetUID);
            // 目标用户是否连接到当前服务
            if(targetUserSocketIsExistsCurrentServer(session)) {
                dirSend(session, param);
            } else {
                saveToUnreadQueue(targetUID, param, time); //1. 将消息，保存到对方的【未读队列】中
                broadcastEvent(targetUID, time); //2. 广播：有人发消息了
            }
        } catch (IOException | EncodeException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    private Boolean targetUserSocketIsExistsCurrentServer(WebSocketSession session) {
        return nonNull(session);
    }

    /**
     * 直接发送
     * @param session 消息目标的 Socket
     * @param param 消息
     * @throws EncodeException 消息编码异常
     * @throws IOException 消息发送异常
     */
    private void dirSend(WebSocketSession session, Param param) throws EncodeException, IOException {
        session.sendMessage(new TextMessage(param.message));
    }

    /**
     * 保存消息到目标用户的未读消息队列
     * @param targetUID 目标用户 UID
     * @param param 消息
     * @param time 发送时间
     */
    private void saveToUnreadQueue(Long targetUID, Param param, Date time) {
        queue.sendMessageThrow(targetUID, TEXT, param.message, time);
    }

    private void broadcastEvent(Long targetUID, Date time) {
        RedisConf.Event event = new RedisConf.Event(api.getLoginUser().getId(), targetUID, TEXT, time);
        log.debug("[MessageService::sendTextMessage] 广播事件 event={}", event);
        redisTemplate.convertAndSend(TOPIC, JSONUtil.toJsonPrettyStr(event));
    }
}
