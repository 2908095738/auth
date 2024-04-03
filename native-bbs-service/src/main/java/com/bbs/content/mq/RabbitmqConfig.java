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

    /**
     * 交换机
     */
    //内容
    public static final String EXCHANGE_TOPICS_BBS_INFORM = "bbs.topic";
    //消息
    public static final String EXCHANGE_TOPICS_CHAT_INFORM = "chat.topic";

    /**
     * 队列
     */
    //点赞
    public static final String QUEUE_INFORM_AGREE = "queue_agree";
    //取消点赞
    public static final String QUEUE_INFORM_DEL_AGREE = "queue_del_agree";
    //评论
    public static final String QUEUE_COMMENT = "queue_comment";
    //删除评论
    public static final String QUEUE_DEL_COMMENT = "queue_del_comment";
    //收藏
    public static final String QUEUE_FAVORITE = "queue_favorite";
    //取消收藏
    public static final String QUEUE_UNFAVORITE = "queue_unfavorite";
    //关注
    public static final String QUEUE_FOLLOW = "queue_follow";
    //取消关注
    public static final String QUEUE_UNFOLLOW = "queue_unfollow";

    /**
     * ROUTING KEY
     */
    //点赞
    public static final String ROUTINGKEY_AGREE = "agree";
    //取消点赞
    public static final String ROUTINGKEY_DEL_AGREE = "del_agree";
    //评论
    public static final String ROUTINGKEY_COMMENT = "comment";
    //删除评论
    public static final String ROUTINGKEY_DEL_COMMENT = "del_comment";
    //收藏
    public static final String ROUTINGKEY_FAVORITE = "favorite";
    //取消收藏
    public static final String ROUTINGKEY_UNFAVORITE = "unfavorite";
    //关注
    public static final String ROUTINGKEY_FOLLOW = "follow";
    //取消关注
    public static final String ROUTINGKEY_UNFOLLOW = "unfollow";

    /**
     * 声明交换机
     */
    //论坛模块
    @Bean(EXCHANGE_TOPICS_BBS_INFORM)
    public Exchange EXCHANGE_TOPICS_BBS_INFORM() {
        //durable(true) 持久化，mq重启之后交换机还在
        return ExchangeBuilder.topicExchange(EXCHANGE_TOPICS_BBS_INFORM).durable(true).build();
    }
    //消息模块
    @Bean(EXCHANGE_TOPICS_CHAT_INFORM)
    public Exchange EXCHANGE_TOPICS_CHAT_INFORM() {
        //durable(true) 持久化，mq重启之后交换机还在
        return ExchangeBuilder.topicExchange(EXCHANGE_TOPICS_CHAT_INFORM).durable(true).build();
    }

    /**
     * 声明QUEUE队列
     */
    //点赞
    @Bean(QUEUE_INFORM_AGREE)
    public Queue QUEUE_INFORM_AGREE() {
        return new Queue(QUEUE_INFORM_AGREE);
    }
    //取消点赞
    @Bean(QUEUE_INFORM_DEL_AGREE)
    public Queue QUEUE_INFORM_DEL_AGREE() {
        return new Queue(QUEUE_INFORM_DEL_AGREE);
    }

    //评论
    @Bean(QUEUE_COMMENT)
    public Queue QUEUE_COMMENT() {
        return new Queue(QUEUE_COMMENT);
    }
    //删除评论
    @Bean(QUEUE_DEL_COMMENT)
    public Queue QUEUE_DEL_COMMENT() {
        return new Queue(QUEUE_DEL_COMMENT);
    }
    //收藏
    @Bean(QUEUE_FAVORITE)
    public Queue QUEUE_FAVORITE() {
        return new Queue(QUEUE_FAVORITE);
    }
    //取消收藏
    @Bean(QUEUE_UNFAVORITE)
    public Queue QUEUE_UNFAVORITE() {
        return new Queue(QUEUE_UNFAVORITE);
    }
    //关注
    @Bean(QUEUE_FOLLOW)
    public Queue QUEUE_FOLLOW() {
        return new Queue(QUEUE_FOLLOW);
    }
    //取消关注
    @Bean(QUEUE_UNFOLLOW)
    public Queue QUEUE_UNFOLLOW() {
        return new Queue(QUEUE_UNFOLLOW);
    }



    /**
     * 队列绑定交换机，指定routingKey
     */


    //点赞
    @Bean
    public Binding BINDING_QUEUE_INFORM_AGREE(@Qualifier(QUEUE_INFORM_AGREE) Queue queue,
                                              @Qualifier(EXCHANGE_TOPICS_CHAT_INFORM) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTINGKEY_AGREE).noargs();
    }
    //取消点赞
    @Bean
    public Binding BINDING_QUEUE_INFORM_DEL_AGREE(@Qualifier(QUEUE_INFORM_DEL_AGREE) Queue queue,
                                                  @Qualifier(EXCHANGE_TOPICS_CHAT_INFORM) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTINGKEY_DEL_AGREE).noargs();
    }

    //评论
    @Bean
    public Binding BINDING_QUEUE_COMMENT(@Qualifier(QUEUE_COMMENT) Queue queue,
                                                  @Qualifier(EXCHANGE_TOPICS_CHAT_INFORM) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTINGKEY_COMMENT).noargs();
    }
    //删除评论
    @Bean
    public Binding BINDING_QUEUE_DEL_COMMENT(@Qualifier(QUEUE_DEL_COMMENT) Queue queue,
                                              @Qualifier(EXCHANGE_TOPICS_CHAT_INFORM) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTINGKEY_DEL_COMMENT).noargs();
    }

    //收藏
    @Bean
    public Binding BINDING_QUEUE_FAVORITE(@Qualifier(QUEUE_FAVORITE) Queue queue,
                                                  @Qualifier(EXCHANGE_TOPICS_CHAT_INFORM) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTINGKEY_FAVORITE).noargs();
    }
    //取消收藏
    @Bean
    public Binding BINDING_QUEUE_UNFAVORITE(@Qualifier(QUEUE_UNFAVORITE) Queue queue,
                                              @Qualifier(EXCHANGE_TOPICS_CHAT_INFORM) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTINGKEY_UNFAVORITE).noargs();
    }

    //关注
    @Bean
    public Binding BINDING_QUEUE_FOLLOW(@Qualifier(QUEUE_FOLLOW) Queue queue,
                                                  @Qualifier(EXCHANGE_TOPICS_CHAT_INFORM) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTINGKEY_FOLLOW).noargs();
    }
    //取消关注
    @Bean
    public Binding BINDING_QUEUE_UNFOLLOW(@Qualifier(QUEUE_UNFOLLOW) Queue queue,
                                              @Qualifier(EXCHANGE_TOPICS_CHAT_INFORM) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTINGKEY_UNFOLLOW).noargs();
    }








}