package com.bbs.content.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.content.entity.OperationLog;
import com.bbs.content.service.OperationLogService;
import com.bbs.vo.BaseParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static com.bbs.Result.success;
import static java.util.Objects.nonNull;

/**
 * 日志
 */
@RestController
@RequestMapping("/log")
public class LogController {

    @Resource
    private OperationLogService operationLogService;



    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class SearchOperationLogParam extends BaseParam {

        private Long userId = 1L;//ThreadLocalUtil.getCurrentUser().getId()
    }

    @GetMapping("/operation")
    public Result<Page<OperationLog>> searchOperationLog(SearchOperationLogParam param) {
        return success(operationLogService.lambdaQuery()
                .eq(nonNull(param.userId), OperationLog::getUserId, param.userId)
                .orderByDesc(OperationLog::getCreateTime)
                .page(param.toPage())
        );
    }
}
