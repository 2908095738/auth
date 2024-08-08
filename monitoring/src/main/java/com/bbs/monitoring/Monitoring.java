package com.bbs.monitoring;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@EnableAdminServer
@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
public class Monitoring {
    public static void main(String[] args) {
        SpringApplication.run(Monitoring.class, args);
    }
}