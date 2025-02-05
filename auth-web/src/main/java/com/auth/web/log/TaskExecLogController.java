package com.auth.web.log;

import com.auth.Result;
import com.auth.log.TaskExecLog;
import com.auth.log.entity.TaskExecHistoryLog;
import com.auth.vo.BasePageParam;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
public class TaskExecLogController {

    @Resource
    private TaskExecLog taskExecLog;

    @GetMapping("/log/task/exec/page")
    public Result<Page<TaskExecHistoryLog>> page(BasePageParam param) {
        return Result.success(taskExecLog.page(param));
    }
}