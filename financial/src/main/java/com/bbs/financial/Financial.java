package com.bbs.financial;

import cn.hutool.extra.spring.EnableSpringUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@MapperScan({"com.bbs.financial.mapper"})
@EnableAsync
@EnableSpringUtil
@EnableCaching
@ComponentScan(value = { "com.bbs.api", "com.bbs.financial"})
@SpringBootApplication
@EnableDiscoveryClient  //Nacos 服务发现
public class Financial {
    public static void main(String[] args) {
        SpringApplication.run(Financial.class, args);
    }
}