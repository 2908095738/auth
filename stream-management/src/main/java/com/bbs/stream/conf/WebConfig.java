package com.bbs.stream.conf;

import com.bbs.stream.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Bean
    public LoginInterceptor createLoginInterceptor() {
        return new LoginInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 排除 swagger 访问的路径配置
        String[] swaggerExcludes = new String[]{
                "/stream-manage-api",
                "/swagger-ui/**",
                "/swagger-resources/**",
                "/webjars/**",
                "/v3/**",
                "/doc.html",
        };

        registry.addInterceptor(createLoginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(swaggerExcludes);
    }
}