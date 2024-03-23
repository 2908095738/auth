package com.auth;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@Slf4j
@MapperScan({"com.auth.mapper"})
@EnableRetry    //启用操作重试
@SpringBootApplication
public class AuthServer implements ApplicationRunner {
    public static void main(String[] args) {
        SpringApplication.run(AuthServer.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("。。。。。。。。。。。。。容器初始化完毕。。。。。。。。。。。。。。");
    }
}