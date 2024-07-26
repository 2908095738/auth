package com.bbs.auth.conf;


import com.bbs.exception.ReLoginException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;

@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {



    @Override
    public boolean preHandle(@NotNull @org.jetbrains.annotations.NotNull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler) throws ReLoginException, IOException {
        String requestURI = request.getRequestURI();
        log.debug("Request URI:{}", requestURI);
        return true;
    }


    /**
     * 接口访问结束后，从ThreadLocal中删除用户信息
     */
    @Override
    public void afterCompletion(@org.jetbrains.annotations.NotNull HttpServletRequest request, @org.jetbrains.annotations.NotNull HttpServletResponse response, @org.jetbrains.annotations.NotNull Object handler, Exception ex) {

    }

}
