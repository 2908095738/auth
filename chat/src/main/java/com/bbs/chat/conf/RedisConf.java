package com.bbs.chat.conf;

import com.bbs.chat.app.chat.Handle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.*;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import java.util.Date;

import static org.springframework.amqp.core.ExchangeTypes.TOPIC;

@Configuration
@EnableRedisHttpSession
public class RedisConf {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Event {

        /**
         * 发送用户 ID
         */
        private Long sourceUID;

        /**
         * 目标用户 ID
         */
        private Long targetUID;

        /**
         * 消息类型（默认文本）
         */
        private Integer type;

        /**
         * 消息发送时间
         */
        private Date time;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(Handle socketHandle) {
        //这个地方 是给messageListenerAdapter 传入一个消息接受的处理器，利用反射的方法调用“MessageReceive1 ”
        return new MessageListenerAdapter(socketHandle, "sendMessageToUser");
    }

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory redisConnectionFactory, MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        // 订阅多个频道
        redisMessageListenerContainer.addMessageListener(listenerAdapter, new PatternTopic(TOPIC));

        return redisMessageListenerContainer;
    }

    /**
     * redisTemplate 序列化使用的 Serializeable, 存储二进制字节码, 所以自定义序列化类
     * @param redisConnectionFactory 连接工厂
     * @return redisTemplate
     */
    @Bean
    public RedisTemplate<String, String> protoStuffTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // redis value使用的序列化器
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        // redis key使用的序列化器
        template.setKeySerializer(new StringRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisSerializationContext<String, Object> redisSerializationContext() {
        RedisSerializationContext.RedisSerializationContextBuilder<String, Object> builder = RedisSerializationContext.newSerializationContext();
        builder.key(StringRedisSerializer.UTF_8);
        builder.value(RedisSerializer.json());
        builder.hashKey(StringRedisSerializer.UTF_8);
        builder.hashValue(StringRedisSerializer.UTF_8);

        return builder.build();
    }

    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {
        RedisSerializationContext<String, Object> serializationContext = redisSerializationContext();
        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }
}
