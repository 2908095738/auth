package com.auth.config.impl.service.impl;

import com.auth.config.Config;
import com.auth.config.enums.ConfigStateEnum;
import com.auth.config.impl.entity.RedisCacheConfig;
import com.auth.config.impl.mapper.RedisCacheConfigMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.base.MPJBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Primary
@Order(1)
@Service
public class RedisCacheConfigServiceImpl extends MPJBaseServiceImpl<RedisCacheConfigMapper, RedisCacheConfig> implements Config.CacheConfig, InitializingBean {

    private static final Map<String, RedisCacheConfig> redisConfigMap = new HashMap<>();

    @Override
    public void afterPropertiesSet() {
        log.info("RedisKey 配置 - 开始加载 Redis Key 配置...");
        List<RedisCacheConfig> configList = lambdaQuery().eq(RedisCacheConfig::getState, ConfigStateEnum.OPEN.getState()).list();
        Map<String, RedisCacheConfig> configMapper = configList.stream().collect(Collectors.toMap(RedisCacheConfig::getCode, config -> config));
        redisConfigMap.putAll(configMapper);
        log.info("RedisKey 配置 - 完成加载！！！数量={}", redisConfigMap.size());
    }

    @Override
    public RedisCacheConfig getKeyConfig(String cacheCode) {
        return redisConfigMap.get(cacheCode);
    }

    @Override
    public Page<RedisCacheConfig> page(Integer current, Integer size) {
        return page(new Page<>(current, size));
    }
}
