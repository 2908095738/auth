package com.bbs.chat.service.impl;

import cn.hutool.json.JSONUtil;
import com.bbs.api.Auth;
import com.bbs.chat.conf.RedisConf;
import com.bbs.chat.app.chat.queue.UserMessageQueue;
import com.bbs.chat.service.MessageService;
import com.bbs.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

import static com.bbs.chat.app.chat.api.SendMessage.TOPIC;


@Slf4j
@Component
public class MessageServiceImpl implements MessageService {

    @Resource
    private UserMessageQueue queue;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private Auth.UserAPI api;

    @Override
    public void sendTextMessage(Long targetUID, String content) throws BusinessException {
        Long loginUID = api.getLoginUser().getId();
        Date sendTime = new Date();
        Integer messageType = NumberUtils.INTEGER_ZERO;

        queue.sendMessageThrow(targetUID, messageType, content, sendTime);
        RedisConf.Event event = new RedisConf.Event(loginUID, targetUID, messageType, sendTime);
        log.debug("[MessageService::sendTextMessage] 发布事件 event={}", event);
        redisTemplate.convertAndSend(TOPIC, JSONUtil.toJsonPrettyStr(event));
    }
}
