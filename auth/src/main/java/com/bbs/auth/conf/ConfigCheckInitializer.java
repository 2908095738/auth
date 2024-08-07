package com.bbs.auth.conf;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigCheckInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        String nacosServerAddr = environment.getProperty("spring.cloud.nacos.config.server-addr");
        String redisAddress = environment.getProperty("spring.redis.host");

        try (FileWriter writer = new FileWriter("/tmp/config-info.txt")) {
            writer.write("Nacos Server Address: " + nacosServerAddr + "\n");
            writer.write("Redisson Redis Address: " + redisAddress + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}