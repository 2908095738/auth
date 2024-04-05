package com.bbs.log.mq.conf;

import com.bbs.enums.log.MQ;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitMQConf {

    /**
     * 1.声明交换机
     * topicExchange的参数说明:
     * param1. 交换机名称
     * param2. 是否持久化 true：持久化，交换机一直保留 false：不持久化，用完就删除
     * param3. 是否自动删除 false：不自动删除 true：自动删除
     */
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(MQ.EXCHANGE_NAME, true, false);
    }

    /**
     * 2. 声明队列
     * param2: 是否持久化 true：持久化 false：不持久化
     */
    @Bean
    public Queue errorLogQueue() {
        return new Queue(MQ.LOG_ERROR_QUEUE, true);
    }

    @Bean
    public Queue warnLogQueue() {
        return new Queue(MQ.LOG_WARN_QUEUE, true);
    }

    /**
     * 3. 队列与交换机绑定
     */
    @Bean
    public Binding errorLogQueueBinding() {
        return BindingBuilder.bind(errorLogQueue()).to(topicExchange()).with("#.log.error.#");
    }

    @Bean
    public Binding warnLogQueueBinding() {
        return BindingBuilder.bind(warnLogQueue()).to(topicExchange()).with("#.log.warn.#");
    }
}
