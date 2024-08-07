package com.clinic.conf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

@Slf4j
public class ConfigCheckInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        checkNacosConf(environment);
        String redisAddress = environment.getProperty("spring.redis.host");
        log.info("环境信息 - Redis: addr={};", redisAddress);
    }

    private static void checkNacosConf(ConfigurableEnvironment environment) {
        String serverAddr = environment.getProperty("spring.cloud.nacos.config.server-addr");
        String username = environment.getProperty("spring.cloud.nacos.config.username");
        String password = environment.getProperty("spring.cloud.nacos.config.password");
        log.info("环境信息 - Nacos: addr={}; username={}; password={};", serverAddr, username, password);
        String nameSpace = environment.getProperty("spring.cloud.nacos.config.namespace");
        String prefix = environment.getProperty("spring.cloud.nacos.config.prefix");
        String fileExtension = environment.getProperty("spring.cloud.nacos.config.file-extension");
        String group = environment.getProperty("spring.cloud.nacos.config.group");
        log.info("环境信息 - Nacos: namespace={}; prefix={}; file-extension={}; group={};", nameSpace, prefix, fileExtension, group);
    }
}