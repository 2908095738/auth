package com.bbs.mall.interceptor;

import com.bbs.exception.ReLoginException;
import com.bbs.mall.util.AuthUtil;
import com.bbs.mall.util.ThreadLocalUtil;
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
//        String token = request.getHeader("Authorization");
//        if (token != null) {
//            AuthUtil.UserAPI.User user = api.getLoginUser();
//            if (nonNull(user)) {
//                log.debug("[LoginInterceptor::afterCompletion] 用户信息：{}", toJSONString(user));
//                ThreadLocalUtil.addCurrentUser(user);
//                return true;
//            }
//        }
//        response.sendError(401, "请重新登陆");
//        return false;
        //TODO 暂时注释方便测试
        return true;
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
