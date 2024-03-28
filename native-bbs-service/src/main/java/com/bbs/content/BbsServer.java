package com.bbs.content;

import cn.hutool.extra.spring.EnableSpringUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@MapperScan({"com.bbs.content.mapper"})
@EnableAsync
@EnableSpringUtil
@SpringBootApplication
public class BbsServer {
    public static void main(String[] args) {
        SpringApplication.run(BbsServer.class, args);
    }
}