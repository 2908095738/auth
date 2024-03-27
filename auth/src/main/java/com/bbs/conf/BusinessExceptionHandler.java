package com.bbs.conf;

import com.clinic.Result;
import com.clinic.exception.ReLoginException;
import com.clinic.exception.BusinessException;
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
