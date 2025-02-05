package com.auth.config.impl.service.impl;

import com.auth.config.Config;
import com.auth.config.enums.ConfigStateEnum;
import com.auth.config.impl.entity.TaskConfig;
import com.auth.config.impl.mapper.TaskConfigMapper;
import com.github.yulichang.base.MPJBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import java.util.List;


@Slf4j
@Primary
@Configuration
public class TaskConfigServiceImpl extends MPJBaseServiceImpl<TaskConfigMapper, TaskConfig> implements Config.TaskConfig {

    @Override
    public List<TaskConfig> list() {
        return lambdaQuery().eq(TaskConfig::getState, ConfigStateEnum.OPEN.getState()).list();
    }
}
