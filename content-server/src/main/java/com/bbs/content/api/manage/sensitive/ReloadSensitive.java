package com.bbs.content.api.manage.sensitive;

import com.bbs.Result;
import com.bbs.content.service.SensitiveWordService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping
public class ReloadSensitive {

    @Resource
    private SensitiveWordService sensitiveWordService;

    @PostMapping("/manage/sensitive/reload")
    public Result<Boolean> reload(@RequestParam(required = false, defaultValue = "false") Boolean direct) {
        return Result.success(sensitiveWordService.reload(direct));
    }
}
