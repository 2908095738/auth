package com.bbs.chat.app.chat;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.annotation.Resource;

@Configuration
@EnableWebSocket
public class Conf implements WebSocketConfigurer {

    @Resource
    private Handle socketHandle;

    @Resource
    private Interceptor interceptor;

    @Override
    public void registerWebSocketHandlers(@NotNull WebSocketHandlerRegistry registry) {
        registry
                .addHandler(socketHandle, "/socket")   //访问路径
                .addInterceptors(interceptor)
                .setAllowedOrigins("*");    //本地调试，关闭跨域校验（线上推荐打开）
    }
}
