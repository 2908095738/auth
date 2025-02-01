package com.auth.config;

import com.auth.config.impl.entity.RedisCacheConfig;
import com.auth.config.impl.entity.RedisLockConfig;
import com.auth.config.impl.entity.SystemConfigItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;


public interface Config {

    interface SystemConfig {

        SystemConfigItem getConfig(String code);

        Page<SystemConfigItem> page(Integer current, Integer size);
    }

    interface CacheConfig {

        RedisCacheConfig getKeyConfig(String cacheCode);

        Page<RedisCacheConfig> page(Integer current, Integer size);
    }

    interface LockConfig {

        RedisLockConfig getConfig(String lockCode);

        Page<RedisLockConfig> page(Integer current, Integer size);
    }

    interface NeedSuperAdminConfig {

        Boolean isNeedSuperAdmin(String apiPath);
    }
}
