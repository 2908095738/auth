package com.auth.config.impl.service.impl;

import com.auth.config.Config;
import com.auth.config.enums.ConfigStateEnum;
import com.auth.config.impl.entity.RedisLockConfig;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.auth.config.impl.mapper.ConfigRedisLockMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Primary
@Service
public class ConfigRedisLockServiceImpl extends ServiceImpl<ConfigRedisLockMapper, RedisLockConfig> implements IService<RedisLockConfig>, Config.LockConfig, InitializingBean {

    private static final Map<String, RedisLockConfig> configMap = new HashMap<>();

    @Override
    public void afterPropertiesSet() {
        log.info("Redis 分布式锁 - 开始加载 Redis 分布式锁配置...");
        List<RedisLockConfig> configList = lambdaQuery().eq(RedisLockConfig::getState, ConfigStateEnum.OPEN.getState()).list();
        Map<String, RedisLockConfig> configMapper = configList.stream().collect(Collectors.toMap(RedisLockConfig::getCode, config -> config));
        configMap.putAll(configMapper);
        log.info("Redis 分布式锁 - 完成加载！！！数量={}", configMap.size());
    }

    @Override
    public RedisLockConfig getConfig(String cacheCode) {
        return configMap.get(cacheCode);
    }

    @Override
    public Page<RedisLockConfig> page(Integer current, Integer size) {
        return page(new Page<>(current, size));
    }
}