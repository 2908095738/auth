package com.bbs.content.mq;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {

    //交换机
    public static final String EXCHANGE_TOPICS_BBS_INFORM="bbs.topic";
    public static final String EXCHANGE_TOPICS_CHAT_INFORM="chat.topic";

    //队列
    public static final String QUEUE_INFORM_WAIT_FILE = "queue_inform_wait_file";
    public static final String QUEUE_INFORM_AGREE = "queue_inform_agree";

    //routingkey
    public static final String ROUTINGKEY_FILE_WAIT="inform.file.wait";
    public static final String ROUTINGKEY_AGREE="inform.#.agree.#";


    //声明论坛模块交换机
    @Bean(EXCHANGE_TOPICS_BBS_INFORM)
    public Exchange EXCHANGE_TOPICS_BBS_INFORM(){
        //durable(true) 持久化，mq重启之后交换机还在
        return ExchangeBuilder.topicExchange(EXCHANGE_TOPICS_BBS_INFORM).durable(true).build();
    }

    //声明消息模块交换机
    @Bean(EXCHANGE_TOPICS_CHAT_INFORM)
    public Exchange EXCHANGE_TOPICS_CHAT_INFORM(){
        //durable(true) 持久化，mq重启之后交换机还在
        return ExchangeBuilder.topicExchange(EXCHANGE_TOPICS_CHAT_INFORM).durable(true).build();
    }

    //声明QUEUE_INFORM_WAIT_FILE队列
    @Bean(QUEUE_INFORM_WAIT_FILE)
    public Queue QUEUE_INFORM_WAIT_FILE(){
        return new Queue(QUEUE_INFORM_WAIT_FILE);
    }


    //声明QUEUE_INFORM_AGREE队列
    @Bean(QUEUE_INFORM_AGREE)
    public Queue QUEUE_INFORM_AGREE(){
        return new Queue(QUEUE_INFORM_AGREE);
    }


    //QUEUE_INFORM_FILE队列绑定交换机，指定routingKey
    @Bean
    public Binding BINDING_QUEUE_INFORM_WAIT_FILE(@Qualifier(QUEUE_INFORM_WAIT_FILE) Queue queue,
                                              @Qualifier(EXCHANGE_TOPICS_BBS_INFORM) Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(ROUTINGKEY_FILE_WAIT).noargs();
    }


    //QUEUE_INFORM_AGREE队列绑定交换机，指定routingKey
    @Bean
    public Binding BINDING_QUEUE_INFORM_AGREE(@Qualifier(QUEUE_INFORM_AGREE) Queue queue,
                                          @Qualifier(EXCHANGE_TOPICS_CHAT_INFORM) Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(ROUTINGKEY_AGREE).noargs();
    }

}