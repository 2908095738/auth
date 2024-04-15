package com.bbs.auth.app.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.auth.entity.System;
import com.bbs.auth.service.SystemService;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping
public class SearchSystem {

    @Resource
    private SystemService service;

    @GetMapping("/system/list")
    public Result<Page<System>> search(@RequestParam Integer current, @RequestParam Integer size) {
        return Result.success(service.lambdaQuery()
                .eq(System::getState, NumberUtils.INTEGER_ZERO)
                .page(new Page<>(current, size))
        );
    }
}
