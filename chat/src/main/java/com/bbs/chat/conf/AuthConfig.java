package com.bbs.chat.conf;

import com.bbs.util.AuthUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * auth 配置
 */
@Configuration
public class AuthConfig {
    @Value("${tokenName}")
    private String tokenName;

    @Value("${authServerIP}")
    private String authServerIP;

    @Value("${authServerPort}")
    private String authServerPort;

    @Bean
    public AuthUtil authUtils(){
        return new AuthUtil(tokenName, authServerIP, authServerPort);
    }
}
