package com.auth.phone.cache.impl.service.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.auth.config.Config;
import com.auth.config.impl.entity.RedisCacheConfig;
import com.auth.phone.cache.PhoneUserIdCache;
import com.auth.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Order(1)
@Primary
@Service
public class PhoneUserIdCacheImpl implements PhoneUserIdCache, InitializingBean {

    @Resource
    private RedisUtil redisUtil;

    private RedisCacheConfig cacheConfig;

    private static final String CACHE_CODE = "phone_user_id";

    @Override
    public void afterPropertiesSet() {
        cacheConfig = SpringUtil.getBean(Config.CacheConfig.class).getKeyConfig(CACHE_CODE);
    }

    @Override
    public Long get(String phone) {
        return redisUtil.get(cacheConfig.generateKey(phone), Long.class);
    }

    @Override
    public Long get(Long phone) {
        return redisUtil.get(cacheConfig.generateKey(phone), Long.class);
    }

    @Override
    public void expire(String phone) {
        redisUtil.expire(cacheConfig.generateKey(phone), cacheConfig.generateRandomTimeout(), cacheConfig.getTimeoutUnit());
    }

    @Override
    public void reload(String phone, Long userId) {
        redisUtil.set(cacheConfig.generateKey(phone), userId, cacheConfig.generateRandomTimeout(), cacheConfig.getTimeoutUnit());
    }

    @Override
    public void remove(String phone) {
        redisUtil.delete(cacheConfig.generateKey(phone));
    }
}
