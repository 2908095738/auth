package com.bbs.chat.app.chat;

import cn.hutool.http.HttpUtil;
import com.bbs.api.Auth;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static java.util.Objects.nonNull;

@Slf4j
@Component
public class Interceptor implements HandshakeInterceptor {

    @Resource
    private Auth.UserAPI api;

    public static String URL_PARAM_TOKEN_KEY = "token";

    public static String SOCKET_ATTR_UID = "uid";

    public static String SOCKET_ATTR_TOKEN = "token";


    @Override
    public boolean beforeHandshake(ServerHttpRequest request, @NotNull ServerHttpResponse response, @NotNull WebSocketHandler wsHandler, @NotNull Map<String, Object> attributes) {
        // 获得请求参数
        Map<String, String> paramMap = HttpUtil.decodeParamMap(request.getURI().getQuery(), StandardCharsets.UTF_8);
        String token = paramMap.get(URL_PARAM_TOKEN_KEY);
        if(StringUtils.isNotBlank(token)) {
            Auth.UserAPI.User loginUser = api.getLoginUser(token);
            if (nonNull(loginUser)) {
                // 放入属性域
                Long id = loginUser.getId();
                attributes.put(SOCKET_ATTR_UID, loginUser.getId());
                attributes.put(SOCKET_ATTR_TOKEN, token);
                log.debug("用户 {} 握手成功！", id);
                return true;
            }
        }
        log.debug("用户登录已失效");
        return false;
    }

    @Override
    public void afterHandshake(@NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response, @NotNull WebSocketHandler wsHandler, Exception exception) {
        log.debug("握手完成");
    }
}
