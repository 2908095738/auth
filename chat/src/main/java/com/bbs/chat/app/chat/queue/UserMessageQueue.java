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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
         * 发送用户 ID
         */
        private Long sourceUID;

        /**
         * 消息类型（默认文本）
         */
        private Integer type;

        /**
         * 消息内容
         */
        private String content;

        /**
         * 消息发送时间
         */
        private Date time;
    }

    public static String queueName(Long sourceUID) {
        return USER_MESSAGE_QUEUE_NAME + sourceUID;
    }

    /**
     * 发送消息
     * @param sourceUID 发送人的 ID
     * @param type 消息类型
     * @param content 消息内容
     * @param time 发送时间
     * @throws BusinessException 消息发送失败
     */
    public void sendMessageThrow(Long sourceUID, Integer type, String content, Date time) throws BusinessException {
        if(!sendMessage(sourceUID, new Message(sourceUID, type, content, time))) {
            log.error("[UserMessageQueue::sendMessage] 发送消息到【用户持久化队列】失败！！！");
            throw new BusinessException();
        }
    }


    public Boolean sendMessage(Long sourceUID, Message message) {
        RQueue<String> queue = redisson.getQueue(queueName(sourceUID));
        log.debug("[UserMessageQueue::sendMessage] 发送消息到【用户持久化队列】sourceUID={}; message={};", sourceUID, message);
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

    /**
     * 获取所有未读消息
     * @return Map<UID, 未读消息>
     */
    public Map<Long, List<Message>> getAllGroupUnreadMessage(Long sourceUID) {
        return getAllUnreadMessage(sourceUID)
                .stream().map(str -> JSONUtil.toBean(str, Message.class))
                .collect(Collectors.groupingBy(Message::getSourceUID));

    }

    public Integer getAllUnreadMessageSize(Long userID) {
        RQueue<String> queue = redisson.getQueue(queueName(userID));
        return queue.size();
    }
}
