package com.bbs.chat;

import cn.hutool.extra.spring.EnableSpringUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@ComponentScan({"com.bbs.api", "com.bbs.chat"})
@MapperScan({"com.bbs.chat.mapper"})
@EnableAsync
@EnableSpringUtil
@SpringBootApplication
public class ChatServer {

    public static void main(String[] args) {
        SpringApplication.run(ChatServer.class, args);
    }

}