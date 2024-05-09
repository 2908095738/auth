package com.bbs.stream;

import cn.hutool.extra.spring.EnableSpringUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@ComponentScan({"com.bbs.api", "com.bbs.stream"})
@MapperScan({"com.bbs.stream.mapper"})
@EnableAsync
@EnableSpringUtil
@SpringBootApplication
public class StreamServer {
    public static void main(String[] args) {
        SpringApplication.run(StreamServer.class, args);
    }
}