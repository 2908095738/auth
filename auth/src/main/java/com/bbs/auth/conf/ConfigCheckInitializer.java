package com.bbs.auth.conf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

@Slf4j
public class ConfigCheckInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        String nacosServerAddr = environment.getProperty("spring.cloud.nacos.config.server-addr");
        String nacosUsername = environment.getProperty("spring.cloud.nacos.config.username");
        String nacosPassword = environment.getProperty("spring.cloud.nacos.config.password");
        log.info("环境信息 - Nacos: addr={}; username={}; password={};", nacosServerAddr, nacosUsername, nacosPassword);
        String nacosNameSpace = environment.getProperty("spring.cloud.nacos.config.namespace");
        String nacosPrefix = environment.getProperty("spring.cloud.nacos.config.prefix");
        String nacosFileExtension = environment.getProperty("spring.cloud.nacos.config.file-extension");
        String nacosGroup = environment.getProperty("spring.cloud.nacos.config.group");
        log.info("环境信息 - Nacos: namespace={}; prefix={}; file-extension={}; group={};", nacosNameSpace, nacosPrefix, nacosFileExtension, nacosGroup);
        String redisAddress = environment.getProperty("spring.redis.host");
        System.out.println("环境信息 - nacosServerAddr: " + nacosServerAddr + " redisAddress: " + redisAddress);
    }
}