package com.auth.log.config;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.IdUtil;
import com.auth.config.Config;
import com.auth.config.enums.TaskExecResultEnum;
import com.auth.config.enums.TaskTimeUnitEnum;
import com.auth.config.impl.entity.TaskConfig;
import com.auth.log.TaskExecLog;
import com.auth.log.entity.TaskExecHistoryLog;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static java.util.Objects.isNull;

@Slf4j
@Component
public class LoadAllTasks implements SchedulingConfigurer {

    @Resource
    private Config.TaskConfig taskConfig;

    @Resource
    private TaskExecLog taskExecHistoryLog;

    // 追踪ID在MDC中的键名
    private static final String TRACE_ID = "TRACE_ID";

    @Bean
    public TaskScheduler taskScheduler() {
        return new ConcurrentTaskScheduler(); //single threaded by default
    }

    @Override
    public void configureTasks(@NotNull ScheduledTaskRegistrar taskRegistrar) {
        log.info("定时任务 - 开始加载定时任务配置...");
        List<TaskConfig> taskConfigList = searchTask();
        taskConfigList.forEach(taskConfig -> taskRegistrar.addCronTask(() -> {
            String traceId = IdUtil.getSnowflakeNextIdStr();
            MDC.put(TRACE_ID, traceId);
            log.info("定时任务 - 任务【{}】频率【{}（{}）】开始执行！！！", taskConfig.getTaskDescriptor(), taskConfig.getExecDescriptor(), taskConfig.getCron());
            TaskExecHistoryLog execHistoryLog;
            try {
                TimeInterval timer = DateUtil.timer();
                Boolean execResult = executeTask(taskConfig);
                long time = timer.interval();
                execHistoryLog = new TaskExecHistoryLog(taskConfig, TaskExecResultEnum.getEnum(execResult), time, TaskTimeUnitEnum.MILLISECONDS, traceId);
                log.info("定时任务 - 任务【{}】执行完成！耗时={}ms;", taskConfig.getTaskDescriptor(), time);
            } catch (Exception e) {
                execHistoryLog = new TaskExecHistoryLog(taskConfig, TaskExecResultEnum.FAIL, traceId);
                log.error("定时任务 - 任务执行失败！", e);
            }
            saveExecHistoryLog(execHistoryLog);
        }, taskConfig.getCron()));
        log.info("定时任务 - 完成加载！！！数量={}", taskConfigList.size());
    }

    private void saveExecHistoryLog(TaskExecHistoryLog execHistoryLog) {
        taskExecHistoryLog.record(execHistoryLog);
    }

    private Boolean executeTask(TaskConfig taskConfig) {
        Object result = ClassUtil.invoke(taskConfig.getClassNameWithMethodName(), new Object[0]);
        if(isNull(result)) {
            return false;
        }
        if(result instanceof Boolean) {
            return (Boolean) result;
        }
        return true;
    }

    private List<TaskConfig> searchTask() {
        return taskConfig.list();
    }
}
