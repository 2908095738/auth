package com.auth.phone.code.cache.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.auth.config.Config;
import com.auth.config.impl.entity.RedisCacheConfig;
import com.auth.phone.code.cache.PhoneCodeCache;
import com.auth.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static java.util.Objects.nonNull;

@Slf4j
@Order(1)
@Service
public class PhoneCodeCacheImpl implements PhoneCodeCache, InitializingBean {

    private static final String CACHE_CODE = "phone_code";

    @Resource
    private RedisUtil redis;

    private RedisCacheConfig cacheConfig;

    @Override
    public void afterPropertiesSet() {
        cacheConfig = SpringUtil.getBean(Config.CacheConfig.class).getKeyConfig(CACHE_CODE);
    }

    @Override
    public void reload(String phone, Integer code) throws IllegalArgumentException {
        String key = cacheConfig.generateKey(phone);
        redis.set(key, code, cacheConfig.getRandomTimeout(), cacheConfig.getTimeoutUnit());
    }

    @Override
    public void remove(String phone) {
        redis.delete(cacheConfig.generateKey(phone));
    }

    @Override
    public Integer get(String phone) {
        Integer code = redis.get(cacheConfig.generateKey(phone), Integer.class);
        if(nonNull(code)) {
            expire(phone);
        }
        return code;
    }

    @Override
    public void expire(String phone) {
        redis.expire(cacheConfig.generateKey(phone), cacheConfig.getRandomTimeout(), cacheConfig.getTimeoutUnit());
    }
}
