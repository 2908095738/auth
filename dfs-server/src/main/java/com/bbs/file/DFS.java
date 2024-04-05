package com.bbs.file;

import cn.hutool.extra.spring.EnableSpringUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

@MapperScan({"com.bbs.file"})
@EnableAsync
@EnableSpringUtil
@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
public class DFS {
    public static void main(String[] args) {
        SpringApplication.run(DFS.class, args);
    }
}