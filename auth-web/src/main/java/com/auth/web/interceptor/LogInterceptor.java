package com.auth.web.interceptor;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

@Component
@Slf4j
public class LogInterceptor implements HandlerInterceptor {

    // 追踪ID在MDC中的键名
    private static final String TRACE_ID = "TRACE_ID";

    @Override
    public boolean preHandle(@NotNull @org.jetbrains.annotations.NotNull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler) {
        // 生成追踪ID，如果客户端传入了追踪ID，则使用客户端传入的追踪ID，否则使用默认的ULID生成
        // 将追踪ID放入MDC中
        MDC.put(TRACE_ID, IdUtil.getSnowflakeNextIdStr());
        return true;
    }


    /**
     * 接口访问结束后，从ThreadLocal中删除用户信息
     */
    @Override
    public void afterCompletion(@org.jetbrains.annotations.NotNull HttpServletRequest request,
                                @org.jetbrains.annotations.NotNull HttpServletResponse response,
                                @org.jetbrains.annotations.NotNull Object handler,
                                Exception ex) {
        // 请求处理完成后，从MDC中移除追踪ID
        MDC.remove(TRACE_ID);
    }

}