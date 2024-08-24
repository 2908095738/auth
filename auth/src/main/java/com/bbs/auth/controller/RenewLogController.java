package com.bbs.auth.controller;

import com.bbs.Result;
import com.bbs.auth.entity.RenewLog;
import com.bbs.auth.service.RenewLogService;
import com.bbs.auth.util.LoginUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class RenewLogController {

    @Resource
    private RenewLogService renewLogService;

    @GetMapping("/renew/log")
    public Result<List<RenewLog>> get() {
        return Result.success(renewLogService.getListByUserId(LoginUser.getId()));
    }

}
