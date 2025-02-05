package com.auth.log;

import com.auth.log.entity.TaskExecHistoryLog;
import com.auth.vo.BasePageParam;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface TaskExecLog {

    Page<TaskExecHistoryLog> page(BasePageParam param);

    void record(TaskExecHistoryLog log);
}
