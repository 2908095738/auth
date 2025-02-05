package com.auth.log.impl;

import com.auth.log.TaskExecLog;
import com.auth.log.entity.TaskExecHistoryLog;
import com.auth.log.mapper.TaskExecHistoryLogMapper;
import com.auth.vo.BasePageParam;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Primary
@Service
public class TaskExecHistoryLogServiceImpl extends ServiceImpl<TaskExecHistoryLogMapper, TaskExecHistoryLog> implements TaskExecLog {
    @Override
    public Page<TaskExecHistoryLog> page(BasePageParam param) {
        return page(new Page<>(param.getCurrent(), param.getSize()));
    }

    @Override
    public void record(TaskExecHistoryLog log) {
        super.save(log);
    }
}
