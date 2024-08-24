package com.bbs.financial.config;

import com.bbs.Result;
import com.bbs.exception.BusinessException;
import com.bbs.exception.NoAccountingSetException;
import com.bbs.exception.ReLoginException;
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
        exception.printStackTrace();
        return Result.failed(exception.getCode(), exception.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = ReLoginException.class)
    public void errorHandler(ReLoginException exception) throws IOException {
        response.sendError(exception.getCode(), exception.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = IllegalArgumentException.class)
    public Result<Object> errorHandler(IllegalArgumentException exception) throws IOException {
        String message = exception.getMessage();
        exception.printStackTrace();
        log.debug("[ExceptionHandler::IllegalArgumentException] error={}", message);
        return Result.failed(400, message);
    }

    @ResponseBody
    @ExceptionHandler(value = NoAccountingSetException.class)
    public Result<Object> errorHandler(NoAccountingSetException exception) throws IOException {
        return Result.failed(501, "没有找到账套！");
    }



}
