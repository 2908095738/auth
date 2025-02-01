package com.auth.config.impl.service.impl;

import com.auth.config.Config;
import com.auth.config.enums.ConfigStateEnum;
import com.auth.config.impl.entity.SystemConfigItem;
import com.auth.config.impl.mapper.SystemBaseConfigMapper;
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
@Order(1)
@Primary
@Service
public class SystemBaseConfigServiceImpl extends MPJBaseServiceImpl<SystemBaseConfigMapper, SystemConfigItem> implements Config.SystemConfig, InitializingBean {

    private static final Map<String, SystemConfigItem> configMap = new HashMap<>();

    @Override
    public void afterPropertiesSet() {
        log.info("系统基础配置 - 开始加载系统基础配置...");
        List<SystemConfigItem> configList = lambdaQuery().eq(SystemConfigItem::getState, ConfigStateEnum.OPEN.getState()).list();
        Map<String, SystemConfigItem> configMapper = configList.stream().collect(Collectors.toMap(SystemConfigItem::getCode, config -> config));
        configMap.putAll(configMapper);
        log.info("系统基础配置 - 完成加载！！！数量={}", configMap.size());
    }


    @Override
    public SystemConfigItem getConfig(String code) {
        return configMap.get(code);
    }

    @Override
    public Page<SystemConfigItem> page(Integer current, Integer size) {
        return page(new Page<>(current, size));
    }
}
