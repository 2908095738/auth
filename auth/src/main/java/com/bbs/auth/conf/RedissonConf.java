package com.bbs.auth.conf;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Slf4j
@Configuration
@RefreshScope
public class RedissonConf {

    @Value("${spring.redis.redisson.config}")
    private String redissonConfig;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient getRedisson() throws IOException {
        Config config = Config.fromYAML(redissonConfig);
        config.setCodec(new JsonJacksonCodec());
        return Redisson.create(config);
    }
}
