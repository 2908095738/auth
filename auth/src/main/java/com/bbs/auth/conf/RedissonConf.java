package com.bbs.auth.conf;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RedissonConf {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.password}")
    private String password;

    @Value("${spring.redis.port}")
    private int port;
    @Bean
    public RedissonClient getRedisson() {
        String address = "redis://" + host + ":" + port;
        log.info("创建 RedissonClient Bean: address={}; password={}", address, password);
        Config config = new Config();
        config.useSingleServer().
                setAddress(address)
                .setPassword(password);
        config.setCodec(new JsonJacksonCodec());
        return Redisson.create(config);
    }
}
