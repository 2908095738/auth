package com.manage;

import cn.hutool.extra.spring.EnableSpringUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@MapperScan({"com.manage.mapper"})
@EnableSpringUtil
@SpringBootApplication
public class ManageServer {
    public static void main(String[] args) {
        SpringApplication.run(ManageServer.class, args);
    }
}