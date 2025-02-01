package com.auth.config.impl.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.IdUtil;
import com.auth.config.Config;
import com.auth.config.impl.entity.TaskConfig;
import com.auth.config.impl.mapper.TaskConfigMapper;
import com.auth.enums.StateEnum;
import com.github.yulichang.base.MPJBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Primary
@Service
public class TaskConfigServiceImpl extends MPJBaseServiceImpl<TaskConfigMapper, TaskConfig> implements Config.TaskConfig, SchedulingConfigurer {

    // 追踪ID在MDC中的键名
    private static final String TRACE_ID = "TRACE_ID";

    @Override
    public void configureTasks(@NotNull ScheduledTaskRegistrar taskRegistrar) {
        log.info("定时任务 - 开始加载定时任务配置...");
        List<TaskConfig> taskConfigList = searchTask();
        taskConfigList.forEach(taskConfig -> taskRegistrar.addCronTask(() -> {
            MDC.put(TRACE_ID, IdUtil.getSnowflakeNextIdStr());
            log.info("定时任务 - 任务【{}】频率【{}（{}）】开始执行！！！", taskConfig.getTaskDescriptor(), taskConfig.getExecDescriptor(), taskConfig.getCron());
            try {
                TimeInterval timer = DateUtil.timer();
                executeTask(taskConfig);
                log.error("定时任务 - 任务【{}】执行完成！耗时={}ms;", taskConfig.getTaskDescriptor(), timer.interval());
            } catch (Exception e) {
                log.error("定时任务 - 任务执行失败！", e);
            }
        }, taskConfig.getCron()));
        log.info("定时任务 - 完成加载！！！数量={}", taskConfigList.size());
    }

    private void executeTask(TaskConfig taskConfig) {
        ClassUtil.invoke(taskConfig.getClassNameWithMethodName(), new Object[0]);
    }

    private List<TaskConfig> searchTask() {
        return lambdaQuery().eq(TaskConfig::getState, StateEnum.USABLE.getCode()).list();
    }
}
