package com.clinic;

import cn.hutool.extra.spring.EnableSpringUtil;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@MapperScan({"com.clinic.mapper"})
@EnableAsync
@EnableSpringUtil
@EnableCaching
@ComponentScan(value = { "com.bbs.api", "com.clinic"})
@SpringBootApplication
@EnableDubbo
@EnableDiscoveryClient  //Nacos 服务发现
public class ClinicServer {
    public static void main(String[] args) {
        SpringApplication.run(ClinicServer.class, args);
    }
}
