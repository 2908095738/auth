package com.auth.web.log;

import com.auth.Result;
import com.auth.log.Log;
import com.auth.log.entity.LoginLog;
import com.auth.vo.BasePageParam;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
public class LoginLogController {

    @Resource
    private Log.Login loginLog;

    @GetMapping("/log/login/page")
    public Result<Page<LoginLog>> page(BasePageParam param) {
        return Result.success(loginLog.page(param));
    }
}
