package com.clinic.app.log.admission;

import cn.hutool.core.util.PageUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.clinic.entity.AdmissionLog;
import com.clinic.mapper.AdmissionLogMapper;
import com.clinic.util.LoginUser;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.Objects.nonNull;

@RestController("searchAdmissionList")
@RequestMapping
public class SearchList extends MPJBaseServiceImpl<AdmissionLogMapper, AdmissionLog> {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Param {

        /**
         * 日志记录 ID
         */
        @NotNull
        private Long id;

        private Integer current;

        private Integer size;

        private Long createTimeLong;
    }
    @GetMapping("/log/admission/list")
    public Result<Page<AdmissionLog>> search(Param param) {
        Date createTime = null;
        if(nonNull(param.createTimeLong)) {
            createTime = new Date(param.createTimeLong);
        }

        List<AdmissionLog> admissionLogs = selectJoinList(AdmissionLog.class, new MPJLambdaWrapper<AdmissionLog>()
                .selectAll(AdmissionLog.class)
                .eq(AdmissionLog::getUserId, LoginUser.getId())
                .eq(nonNull(createTime), AdmissionLog::getCreateTime, createTime)
        );
        int totalPage = PageUtil.totalPage(admissionLogs.size(), param.getSize());
        Page<AdmissionLog> result = new Page<>(param.getCurrent(), param.getSize(), totalPage);
        List<List<AdmissionLog>> partition = ListUtils.partition(admissionLogs, param.getSize());
        List<AdmissionLog> limitLog = new ArrayList<>();
        if(partition.size() <= param.getCurrent()) {
            limitLog = partition.get(param.getCurrent() - 1);
        }
        result.setRecords(limitLog);
        return Result.success(result);
    }
}