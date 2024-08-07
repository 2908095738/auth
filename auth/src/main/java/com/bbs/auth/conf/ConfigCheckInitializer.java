package com.bbs.auth.conf;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

public class ConfigCheckInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        String nacosServerAddr = environment.getProperty("spring.cloud.nacos.config.server-addr");
        String redisAddress = environment.getProperty("spring.redis.host");
        System.out.println("nacosServerAddr: " + nacosServerAddr + " redisAddress: " + redisAddress);
    }
}