package com.bbs.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.auth.entity.RenewLog;
import com.bbs.auth.service.RenewLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class RenewLogController {

    @Resource
    private RenewLogService renewLogService;

    @GetMapping("by/renew/log")
    public Result<Page<RenewLog>> get(Integer current, Integer size) {
        return Result.success(renewLogService.page(new Page<>(current,size)));
    }

}
