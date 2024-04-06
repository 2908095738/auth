package com.bbs.content.conf;


import com.bbs.content.interceptor.LoginInterceptor;
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

    @Value("${news.file.path}")
    private String filePath;

    @Value("${news.file.down.prefix}")
    private String fileDownPrefix;

    @Value("${news.image.path}")
    private String imagePath;

    @Value("${news.image.down.prefix}")
    private String imageDownPrefix;

    @Value("${news.video.path}")
    private String videoPath;

    @Value("${news.video.down.prefix}")
    private String videoDownPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry)
    {

        // 本地文件上传路径，映射
        registry
                .addResourceHandler(fileDownPrefix + "/**", imageDownPrefix + "/**",videoDownPrefix + "/**")
                .addResourceLocations("file:"+ File.separator + filePath , "file:"  + File.separator + imagePath,"file:" + File.separator + videoPath)
        ;
    }

    /**
     * 开启跨域
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 设置允许跨域的路由
//        registry.addMapping(fileDownPrefix  + "/**")
        registry.addMapping(  "/**")
                // 设置允许跨域请求的域名
                .allowedOrigins("*")
                // 设置允许的方法
                .allowedMethods("GET")
                .allowedMethods("POST")
                ;
    }

    @Bean
    public LoginInterceptor createLoginInterceptor() {
        return new LoginInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(createLoginInterceptor())
//                .addPathPatterns("/**")
//                .excludePathPatterns("/news/upload")
//                .excludePathPatterns(fileDownPrefix + "/**")
//                .excludePathPatterns(imageDownPrefix + "/**")
//                .excludePathPatterns(videoDownPrefix + "/**");
    }
}

