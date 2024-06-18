package com.bbs.content;

import cn.hutool.extra.spring.EnableSpringUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@MapperScan({"com.bbs.content.mapper"})
@EnableAsync
@EnableSpringUtil
@ComponentScan(value = { "com.bbs.api", "com.bbs.content"})
@SpringBootApplication
public class Content {
    public static void main(String[] args) {
        SpringApplication.run(Content.class, args);
    }
}