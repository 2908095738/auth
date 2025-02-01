package com.auth.config.interceptor.checkSuperAdmin;

import cn.hutool.core.annotation.AnnotationUtil;
import com.auth.config.NeedSuperAdmin;
import com.auth.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static java.util.Objects.nonNull;

@Slf4j
@Component
public class CheckSuperAdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler) throws IOException {
        if(handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            if(getClassNeedSuperAdminAnnotationNotNull(handlerMethod) || methodNeedSuperAdminAnnotationNotNull(handlerMethod)) {
                if(User.LoginUserUtil.loginUserNotIsAdmin()) {
                    response.sendError(400,"您没有权限访问该 API");
                    return false;
                }
            }
        }
        return true;
    }

    private Boolean getClassNeedSuperAdminAnnotationNotNull(HandlerMethod handlerMethod) {
        return nonNull(AnnotationUtil.getAnnotation(handlerMethod.getBean().getClass(), NeedSuperAdmin.class));
    }

    private Boolean methodNeedSuperAdminAnnotationNotNull(HandlerMethod handlerMethod) {
        return nonNull(AnnotationUtil.getAnnotation(handlerMethod.getMethod(), NeedSuperAdmin.class));
    }
}
