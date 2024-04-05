package com.bbs.file.conf;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MQConf {

    @Bean
    public RabbitTemplate createRabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory);
        //设置开启消息推送结果回调
        rabbitTemplate.setMandatory(true);
        //设置ConfirmCallback回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            log.info("==============ConfirmCallback start ===============");
            log.info("回调数据：{}", correlationData);
            log.info("确认结果：{}", ack);
            log.info("返回原因：{}", cause);
            log.info("==============ConfirmCallback end =================");
        });
        //设置ReturnCallback回调
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            log.info("==============ReturnCallback start ===============");
            log.info("发送消息：{}", JSONUtil.toJsonStr(message));
            log.info("结果状态码：{}", replyCode);
            log.info("结果状态信息：{}", replyText);
            log.info("交换机：{}", exchange);
            log.info("路由key：{}", routingKey);
            log.info("==============ReturnCallback end =================");
        });
        return rabbitTemplate;
    }
}
