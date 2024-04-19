package com.bbs.chat.app.chat.cache;

import cn.hutool.json.JSONUtil;
import com.bbs.api.Auth;
import com.bbs.chat.app.chat.queue.UserMessageQueue;
import com.bbs.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.*;

/**
 * 聊天列表缓存
 * ps: source -> target
 */
@Slf4j
@Component
public class ChatListCache {

    private static final String KEY_PREFIX = "chat:list:";

    private static final String LAST_MESSAGE_KEY_PREFIX = "chat:message:last";

    private static final String split = ":";

    @Resource(name = "protoStuffTemplate")
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private Auth.UserAPI api;

    private String key(Long uid) {
        return KEY_PREFIX + uid;
    }

    private String lastMSGKey(Long currentUID) {
        return LAST_MESSAGE_KEY_PREFIX + currentUID;
    }

    public void newChat(Long sourceUID, Long targetUID, UserMessageQueue.Message message) {
        newChat(sourceUID, targetUID, message, INTEGER_ONE);
    }

    public void newChat(Long sourceUID, Long targetUID, UserMessageQueue.Message message, Integer unreadMessageNumber) {
        redisTemplate.opsForZSet().incrementScore(key(sourceUID), targetUID.toString(), INTEGER_ZERO);
        redisTemplate.opsForHash().put(lastMSGKey(sourceUID), targetUID, JSONUtil.toJsonPrettyStr(message));
        redisTemplate.opsForZSet().incrementScore(key(targetUID), sourceUID.toString(), unreadMessageNumber);
        redisTemplate.opsForHash().put(lastMSGKey(targetUID), sourceUID, JSONUtil.toJsonPrettyStr(message));
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Chat {
        private Auth.UserAPI.User user;
        private Integer unreadMessageNumber;
        private UserMessageQueue.Message lastMessage;
    }

    /**
     * 获取用户聊天对象列表
     * @param uid 用户 ID
     * @return Map<聊天对象ID, 未读信息数量>
     */
    public List<Chat> getChatList(Long uid) {
        List<Chat> result = new ArrayList<>();
        Set<ZSetOperations.TypedTuple<String>> all = redisTemplate.opsForZSet().rangeWithScores(key(uid), INTEGER_ZERO, -INTEGER_ONE);
        if(nonNull(all)) {
            Map<Long, Chat> chatTargetIDAndChatMap = new HashMap<>();
            Set<Long> chatTargetIDs = new HashSet<>();
            for (ZSetOperations.TypedTuple<String> item : all) {
                Long targetUID = Long.valueOf(Objects.requireNonNull(item.getValue()));
                Object lastMessageObj = redisTemplate.opsForHash().get(lastMSGKey(uid), targetUID);
                if(isNull(lastMessageObj)) throw new BusinessException("用户最后聊天丢失"); //TODO 查询数据库中最后一次聊天
                UserMessageQueue.Message lastMessage = JSONUtil.toBean(lastMessageObj.toString(), UserMessageQueue.Message.class);
                chatTargetIDs.add(targetUID);
                Chat chat = new Chat(
                        new Auth.UserAPI.User(),
                        Objects.requireNonNull(item.getScore()).intValue(),
                        lastMessage
                );
                result.add(chat);
                chatTargetIDAndChatMap.put(targetUID, chat);
            }

            if(chatTargetIDs.size() > INTEGER_ZERO) {
                List<Auth.UserAPI.User> chatTargets = api.getUserList(new ArrayList<>(chatTargetIDs));
                chatTargets.forEach(user -> chatTargetIDAndChatMap.get(user.getId()).setUser(user));
            }
        }

        return result;
    }

    public Integer getUnreadMessageSize(Long uid) {
        String key = key(uid);
        Set<ZSetOperations.TypedTuple<String>> chats = redisTemplate.opsForZSet().rangeWithScores(key, INTEGER_ZERO, -INTEGER_ONE);
        if(nonNull(chats)) {
            return chats.stream().map(ZSetOperations.TypedTuple::getScore).filter(Objects::nonNull).mapToInt(Double::intValue).sum();
        }
        return INTEGER_ZERO;
    }

    public void cleanUnreadMessageFlag(Long currentUID, Long targetUID) {
        redisTemplate.opsForZSet().add(key(currentUID), targetUID.toString(), DOUBLE_ZERO);
    }
}
