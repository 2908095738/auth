package com.bbs.log.mq.consumer;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RabbitListener(queues = {"log-error-topic-queue"})
public class ErrorLogQueueConsumer {

    @RabbitHandler
    public void emailMessage(String msg, Channel channel, Message message) throws IOException {
        try {
            log.error("Rabbit - Error Log topic: 接收到消息={}", msg);

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            if (message.getMessageProperties().getRedelivered()) {
                log.error("Rabbit - Error Log topic: 消息已重复处理失败,拒绝再次接收...");
                //basicReject: 拒绝消息，与basicNack区别在于不能进行批量操作，其他用法很相似 false表示消息不再重新进入队列
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false); // 拒绝消息
            } else {
                log.error("Rabbit - Error Log topic: 消息即将再次返回队列处理...");
                // basicNack:表示失败确认，一般在消费消息业务异常时用到此方法，可以将消息重新投递入队列
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }
        }
    }
}
