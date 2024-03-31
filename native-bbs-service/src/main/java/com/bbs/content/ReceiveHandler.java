package com.bbs.content;

import com.alibaba.fastjson2.JSON;
import com.bbs.content.dto.MqAgreeDto;
import com.bbs.content.util.RabbitmqConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReceiveHandler {

    //监听队列
    @RabbitListener(queues = {RabbitmqConfig.EXCHANGE_TOPICS_BBS_INFORM})
    public void receive_bbs(Message message){


    }


    //监听队列
    @RabbitListener(queues = {RabbitmqConfig.QUEUE_INFORM_AGREE})
    public void receive_agree(Message message){

        log.debug("message:{}",JSON.parseObject(message.getBody(), MqAgreeDto.class));

    }




}