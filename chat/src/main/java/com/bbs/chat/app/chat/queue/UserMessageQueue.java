package com.bbs.chat.app.chat.queue;

import cn.hutool.json.JSONUtil;
import com.bbs.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

@Slf4j
@Component
public class UserMessageQueue {

    @Resource
    private RedissonClient redisson;

    private static final String USER_MESSAGE_QUEUE_NAME = "CHAT:QUEUE:MESSAGE:USER:";


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {

        /**
         * 来源用户 ID
         */
        private Long sourceUID;

        /**
         * 来源用户 Token
         */
        private String token;

        /**
         * 目标用户 ID
         */
        private Long targetUID;

        /**
         * 消息类型（默认文本）
         */
        private Integer type;

        /**
         * 消息内容
         */
        private Object content;

        /**
         * 消息发送时间
         */
        private String time;

        public Message(Long sourceUID, Long targetUID, Integer type, Object content, String time) {
            this.sourceUID = sourceUID;
            this.targetUID = targetUID;
            this.type = type;
            this.content = content;
            this.time = time;
        }
    }

    public static String queueName(Long sourceUID) {
        return USER_MESSAGE_QUEUE_NAME + sourceUID;
    }

    /**
     * 发送消息
     * @param targetUID 目标用户 ID
     * @throws BusinessException 消息发送失败
     */
    public void sendMessageThrow(Long targetUID, Message message) throws BusinessException {
        if(!sendMessage(targetUID, message)) {
            log.error("[UserMessageQueue::sendMessage] 发送消息到【用户持久化队列】失败！！！");
            throw new BusinessException();
        }
    }


    public Boolean sendMessage(Long targetUID, Message message) {
        RQueue<String> queue = redisson.getQueue(queueName(targetUID));
        log.debug("[UserMessageQueue::sendMessage] 发送消息到【用户持久化队列】targetUID={}; message={};", targetUID, message);
        return queue.add(JSONUtil.toJsonPrettyStr(message));
    }

    /**
     * 获取所有未读消息
     * @return 所有未读消息
     */
    public List<String> getAllUnreadMessage(Long sourceUID) {
        RQueue<String> queue = redisson.getQueue(queueName(sourceUID));
        List<String> allMessageStr = queue.poll(queue.size());
        if(nonNull(allMessageStr) && allMessageStr.size() > NumberUtils.INTEGER_ZERO) {
            return allMessageStr;
        }
        return new ArrayList<>();
    }

    public String getUnreadMessage(Long sourceUID) {
        RQueue<String> queue = redisson.getQueue(queueName(sourceUID));
        return queue.poll();
    }
}
