package com.bbs.auth.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class MQUtil {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void send(String exchange, String routingKey, String msg){
        //发送消息
        rabbitTemplate.convertAndSend(exchange, routingKey, msg);
    }
}
