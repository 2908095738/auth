package com.bbs.content.conf;

import com.bbs.Result;
import com.bbs.exception.BusinessException;
import com.bbs.exception.ReLoginException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@ControllerAdvice
public class BusinessExceptionHandler {
    @ResponseBody
    @ExceptionHandler(value = BusinessException.class)
    public Result<Object> errorHandler(BusinessException exception) {
        return Result.failed(exception.getCode(), exception.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = ReLoginException.class)
    public Result<Object> errorHandler(ReLoginException exception) {
        return Result.failed(exception);
    }
}
