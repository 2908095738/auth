package com.bbs.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.auth.entity.RenewLog;
import com.bbs.auth.service.RenewLogService;
import com.bbs.auth.service.UserService;
import com.bbs.exception.ReLoginException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static java.util.Objects.nonNull;

/**
 * 支付记录
 */
@RestController
public class RenewLogController {

    @Resource
    private RenewLogService renewLogService;

    @Resource
    private UserService userService;

    @GetMapping("/back/renew/log")
    public Result<Page<RenewLog>> get(@RequestParam(required = false) Long userId, Integer current, Integer size) throws IllegalArgumentException, ReLoginException {
        userService.loginUserIsAdmin();
        return Result.success(renewLogService.lambdaQuery()
                .eq(nonNull(userId), RenewLog::getCreateBy, userId)
                .page(new Page<>(current,size))
        );
    }

}
