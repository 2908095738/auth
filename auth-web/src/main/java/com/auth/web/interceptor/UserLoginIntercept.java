package com.auth.web.interceptor;

import com.auth.token.Token;
import com.auth.token.impl.dto.UserLoginToken;
import com.auth.user.User;
import com.auth.user.impl.config.threadlocal.LoginUserThreadLocal;
import com.auth.user.dto.UserDTO;
import com.auth.user.exception.UserNotLoginException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录拦截器
 * @author ext.luchenlin5
 */
@Slf4j
@Component
public class UserLoginIntercept implements HandlerInterceptor {

    @Resource
    private Token.ParseUserLoginToken parseUserLoginToken;

    @Resource
    private User.Search searchUser;

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler) throws UserNotLoginException {
        if(IgnoreConfig.match(request.getRequestURI())){
            return true;
        }
        try {
            UserLoginToken token = parseUserLoginToken.parse(request);
            UserDTO user = searchUser.byId(token.getUid());
            LoginUserThreadLocal.set(user);
            return true;
        } catch (Exception e) {
            log.warn("[UserServiceImpl::loginUser] 用户登录信息获取异常 ", e);
            throw new UserNotLoginException();
        }
    }


    @Override
    public void afterCompletion(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler, Exception ex) {
        // 接口访问结束后，从 ThreadLocal 中删除用户信息
        LoginUserThreadLocal.remove();
    }
}
