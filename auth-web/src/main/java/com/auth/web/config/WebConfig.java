package com.auth.web.config;


import com.auth.web.interceptor.IgnoreConfig;
import com.auth.web.interceptor.UserLoginIntercept;
import com.auth.web.interceptor.LogInterceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.mobile.device.DeviceResolverHandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private UserLoginIntercept userLoginIntercept;

    @Override
    public void addResourceHandlers(@NotNull ResourceHandlerRegistry registry) {

    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("PUT", "DELETE","GET","POST")
                .allowedHeaders("*")
                .exposedHeaders("access-control-allow-headers",
                        "access-control-allow-methods",
                        "access-control-allow-origin",
                        "access-control-max-age",
                        "X-Frame-Options")
                .allowCredentials(false).maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userLoginIntercept).addPathPatterns("/**").excludePathPatterns(IgnoreConfig.getIgnoreApiPaths());
        registry.addInterceptor(new DeviceResolverHandlerInterceptor());
        registry.addInterceptor(new LogInterceptor());
    }
}

