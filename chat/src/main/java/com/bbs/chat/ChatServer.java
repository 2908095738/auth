package com.bbs.chat;

import cn.hutool.extra.spring.EnableSpringUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@MapperScan({"com.bbs.chat.mapper"})
@EnableAsync
@EnableSpringUtil
@SpringBootApplication
public class ChatServer {

    public static void main(String[] args) {
        SpringApplication.run(ChatServer.class, args);
    }

}