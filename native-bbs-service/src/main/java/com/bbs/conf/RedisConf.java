package com.bbs.conf;

import com.bbs.auth.conf.ProtostuffSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableRedisHttpSession
public class RedisConf {
    /**
     * redisTemplate 序列化使用的Serializeable, 存储二进制字节码, 所以自定义序列化类
     * @Rparam redisConnectionFactory
     * @return redisTemplate
     */
    @Bean
    public RedisTemplate<String, String> protoStuffTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // redis value使用的序列化器
        template.setValueSerializer(new ProtostuffSerializer());
        // redis key使用的序列化器
        template.setKeySerializer(new StringRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
}
