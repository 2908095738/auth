package com.bbs.content.mq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Map;

@Component
@Slf4j
public class ReceiveHandler {

    /**
     * 监听队列,压缩文件
     */
//    @RabbitListener(queues = {RabbitmqConfig.QUEUE_INFORM_WAIT_FILE})
    public void receive_bbs(Message message, @Headers Map<String, Object> header, Channel channel) {
        Long deliveryTag = (Long) header.get(AmqpHeaders.DELIVERY_TAG);
        try {

            //处理消息：手动回复ack:deliveryTag：从header中获取是哪一条消息（消息编号）,false：不批量回复
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            e.printStackTrace();
            try {
                //回复nack,并重新发送
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    //监听队列
//    @RabbitListener(queues = {RabbitmqConfig.QUEUE_INFORM_AGREE})
//    public void receive_agree(Message message){
//
//        log.debug("message:{}",JSON.parseObject(message.getBody(), MqAgreeDto.class));
//
//    }

}