package com.clinic.conf;


import com.bbs.api.auth.User;
import com.bbs.api.auth.UserAPI;
import com.bbs.exception.ReLoginException;
import com.clinic.util.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;

import static java.util.Objects.nonNull;

@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    @DubboReference
    private UserAPI userAPI;

    @Value("${auth.api.verify.token}")
    private String tokenName;


    @Override
    public boolean preHandle(@NotNull @org.jetbrains.annotations.NotNull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler) throws ReLoginException, IOException {
        log.debug("拦截器request.getRequestURI(){}",request.getRequestURI());
        if(request.getRequestURI().contains("/weixin/pay/notification")){
            return true;
        }
        User user;
        try {
            user = userAPI.getUserByToken(request.getHeader(tokenName));
        } catch (Exception e) {
            throw new ReLoginException();
        }
        if (nonNull(user)){
            LoginUser.set(user);
            return true;
        }
        response.sendError(401,"请重新登陆");
        return false;
    }


    /**
     * 接口访问结束后，从ThreadLocal中删除用户信息
     */
    @Override
    public void afterCompletion(@org.jetbrains.annotations.NotNull HttpServletRequest request, @org.jetbrains.annotations.NotNull HttpServletResponse response, @org.jetbrains.annotations.NotNull Object handler, Exception ex) {
        LoginUser.remove();
    }

}
