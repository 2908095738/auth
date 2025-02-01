package com.auth.web.config;

import com.auth.Result;
import com.auth.config.Config;
import com.auth.config.impl.entity.SystemConfigItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class SystemConfigManager {

    @Resource
    private Config.SystemConfig systemConfig;


    @GetMapping("/config/system/page")
    public Result<Page<SystemConfigItem>> page(
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "10") Integer size
    ) {
        return Result.success(systemConfig.page(current, size));
    }
}
