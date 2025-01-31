package com.auth.web.config;

import cn.hutool.http.HttpStatus;
import cn.hutool.jwt.JWTException;
import com.auth.Result;
import com.auth.exception.BusinessException;
import com.auth.token.impl.exception.UserTokenParseException;
import com.auth.user.exception.UserNotLoginException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@ControllerAdvice
public class SystemExceptionHandler {


    @ResponseBody
    @ExceptionHandler(value = BusinessException.class)
    public Result<Object> errorHandler(BusinessException exception) {
        exception.printStackTrace();
        return Result.failed(exception.getCode(), exception.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = { UserNotLoginException.class, JWTException.class, UserTokenParseException.class})
    public Result<Object> errorHandler(Exception e, HttpServletResponse response) {
        log.warn("全局异常拦截 - 拦截登录相关异常！msg={}", e.getMessage());
        response.setStatus(HttpStatus.HTTP_UNAUTHORIZED);
        return Result.success(HttpStatus.HTTP_UNAUTHORIZED, "用户未登录");
    }

    @ResponseBody
    @ExceptionHandler(value = IllegalArgumentException.class)
    public Result<Object> errorHandler(IllegalArgumentException ignoredException) {
        return Result.success(400, ignoredException.getMessage());
    }
}
