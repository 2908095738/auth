package com.bbs.conf;


import com.bbs.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {


    @Value("${prescription.file.path}")
    private String filePath;

    @Value("${prescription.file.down.prefix}")
    private String fileDownPrefix;

    @Value("${prescription.image.path}")
    private String imagePath;

    @Value("${prescription.image.down.prefix}")
    private String imageDownPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry)
    {

        // 本地文件上传路径，映射
        registry
                .addResourceHandler(fileDownPrefix + "/**", imageDownPrefix + "/**")
                .addResourceLocations("file:" + filePath + File.separator, "file:" + imagePath + File.separator)
        ;
    }

    /**
     * 开启跨域
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 设置允许跨域的路由
        registry.addMapping(fileDownPrefix  + "/**")
                // 设置允许跨域请求的域名
                .allowedOrigins("*")
                // 设置允许的方法
                .allowedMethods("GET");
    }

    @Bean
    public LoginInterceptor createLoginInterceptor() {
        return new LoginInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(createLoginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(fileDownPrefix + "/**")
                .excludePathPatterns(imageDownPrefix + "/**");
    }
}

