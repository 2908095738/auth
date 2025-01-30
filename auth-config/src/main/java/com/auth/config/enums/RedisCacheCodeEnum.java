package com.auth.config.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RedisCacheCodeEnum {

    REDIS_CACHE_USER("redis_cache_user", "Redis 缓存：用户信息")
    ;

    private final String code;

    private final String msg;
}
