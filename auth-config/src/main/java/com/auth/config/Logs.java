package com.auth.config;

import com.auth.config.impl.entity.TaskExecHistoryLog;

public interface Logs {

    interface TaskExecHistory {

        void record(TaskExecHistoryLog log);
    }
}
