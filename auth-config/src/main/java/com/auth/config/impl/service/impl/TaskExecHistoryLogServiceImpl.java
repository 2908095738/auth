package com.auth.config.impl.service.impl;

import com.auth.config.Logs;
import com.auth.config.impl.entity.TaskExecHistoryLog;
import com.auth.config.impl.mapper.TaskExecHistoryLogMapper;
import com.github.yulichang.base.MPJBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Primary
@Service
public class TaskExecHistoryLogServiceImpl extends MPJBaseServiceImpl<TaskExecHistoryLogMapper, TaskExecHistoryLog> implements Logs.TaskExecHistory {
    @Override
    public void record(TaskExecHistoryLog log) {
        super.save(log);
    }
}
