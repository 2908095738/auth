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

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(fileDownPrefix  + "/**")
//        registry.addMapping("/**")
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

    @Bean
    public LoginInterceptor createLoginInterceptor() {
        return new LoginInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(createLoginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/admin/content"+ "/**")
                .excludePathPatterns("/content/query"+ "/**")
                .excludePathPatterns(fileDownPrefix + "/**")
                .excludePathPatterns(imageDownPrefix + "/**")
                .excludePathPatterns(videoDownPrefix + "/**");
    }
}

