package com.auth.web.config;

import com.auth.Result;
import com.auth.config.Config;
import com.auth.config.impl.entity.RedisCacheConfig;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class RedisCacheManager {

    @Resource
    private Config.CacheConfig cacheConfig;

    @GetMapping("/config/cache/redis/page")
    public Result<Page<RedisCacheConfig>> page(
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "10") Integer size
    ) {
        return Result.success(cacheConfig.page(current, size));
    }
}
