package com.bbs.chat.interceptor;


import com.bbs.chat.util.AuthUtil;
import com.bbs.chat.util.ThreadLocalUtil;
import com.bbs.exception.ReLoginException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;

import static com.alibaba.fastjson.JSON.toJSONString;
import static java.util.Objects.nonNull;

@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    @Resource
    private AuthUtil.UserAPI api;


    @Override
    public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws ReLoginException, IOException {
        String token = request.getHeader("Authorization");
        if (token != null) {
            AuthUtil.UserAPI.User user = api.getLoginUser();
            if (nonNull(user)) {
                ThreadLocalUtil.addCurrentUser(user);
                return true;
            }
        }
        response.sendError(401, "请重新登陆");
        return false;
    }


    /**
     * 接口访问结束后，从ThreadLocal中删除用户信息
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        log.info("[LoginInterceptor::afterCompletion] 业务处理结束，开始回收资源：ThreadLocal...");
        ThreadLocalUtil.remove();
    }


}
