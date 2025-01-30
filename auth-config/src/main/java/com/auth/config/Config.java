package com.auth.config;

import com.auth.config.impl.entity.RedisCacheConfig;
import com.auth.config.impl.entity.RedisLockConfig;
import com.auth.config.impl.entity.SystemConfigItem;


public interface Config {

    interface SystemConfig {

        SystemConfigItem getConfig(String code);
    }

    interface CacheConfig {

        RedisCacheConfig getKeyConfig(String cacheCode);
    }

    interface LockConfig {

        RedisLockConfig getConfig(String lockCode);
    }
}
