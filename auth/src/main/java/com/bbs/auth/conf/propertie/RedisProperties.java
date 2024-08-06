package com.bbs.auth.conf.propertie;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
public class RedisProperties {

    private String host = "localhost";

    private Integer port = 6379;

    private String password = "";
}
