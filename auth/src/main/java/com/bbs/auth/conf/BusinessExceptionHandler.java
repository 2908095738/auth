package com.bbs.auth.conf;

import com.bbs.Result;
import com.bbs.exception.ReLoginException;
import com.bbs.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@ControllerAdvice
public class BusinessExceptionHandler {

    @Resource
    private HttpServletResponse response;

    @ResponseBody
    @ExceptionHandler(value = BusinessException.class)
    public Result<Object> errorHandler(BusinessException exception) {
        return Result.failed(exception.getCode(), exception.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = ReLoginException.class)
    public void errorHandler(ReLoginException exception) throws IOException {
        response.sendError(exception.getCode(), exception.getMessage());
    }
}
