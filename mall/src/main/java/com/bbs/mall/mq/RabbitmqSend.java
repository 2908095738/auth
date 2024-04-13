package com.bbs.mall.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 发送
 */
@Slf4j
@Component
public class RabbitmqSend {

    private RabbitTemplate rabbitTemplate;

    public void send(String exchange, String routingKey, String msg){
        //发送消息
        rabbitTemplate.convertAndSend(exchange, routingKey, msg);
    }


    @Resource
    public void setRabbitTemplate(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
}