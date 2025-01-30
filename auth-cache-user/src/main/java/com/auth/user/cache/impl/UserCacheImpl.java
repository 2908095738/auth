package com.auth.user.cache.impl;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.auth.config.Config;
import com.auth.config.impl.entity.RedisCacheConfig;
import com.auth.user.cache.UserCache;
import com.auth.user.dto.UserDTO;
import com.auth.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.hutool.json.JSONUtil.toJsonPrettyStr;
import static java.util.Objects.nonNull;

@Slf4j
@Order(1)
@Service
public class UserCacheImpl implements UserCache, InitializingBean {

    private static final String CACHE_CODE = "redis_cache_user";

    @Resource
    private RedisUtil redis;

    private RedisCacheConfig cacheConfig;

    @Override
    public void afterPropertiesSet() {
        cacheConfig = SpringUtil.getBean(Config.CacheConfig.class).getKeyConfig(CACHE_CODE);
    }


    @Override
    public void reload(UserDTO user) throws IllegalArgumentException {
        String key = cacheConfig.generateKey(user.getId());
        redis.set(key, toJsonPrettyStr(user), cacheConfig.getRandomTimeout(), cacheConfig.getTimeoutUnit());
    }

    @Override
    public void remove(Long userId) {
        redis.delete(cacheConfig.generateKey(userId));
    }

    @Override
    public UserDTO get(Long uid) {
        UserDTO user = redis.get(cacheConfig.generateKey(uid), UserDTO.class);
        if(nonNull(user)) {
            expire(uid);
        }
        return user;
    }

    @Override
    public List<UserDTO> get(List<Long> ids) {
        List<String> idStrList = ids.stream().filter(Objects::nonNull).map(cacheConfig::generateKey).collect(Collectors.toList());
        return redis.multiGet(idStrList).stream()
                .filter(Objects::nonNull)
                .map(str -> JSONUtil.toBean(str, UserDTO.class))
                .peek(user -> expire(user.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public void expire(Long userId) {
        redis.expire(cacheConfig.generateKey(userId), cacheConfig.getRandomTimeout(), cacheConfig.getTimeoutUnit());
    }
}