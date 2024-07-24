package com.clinic.app.log.admission;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.clinic.entity.AdmissionLog;
import com.clinic.entity.Pay;
import com.clinic.mapper.AdmissionLogMapper;
import com.clinic.util.LoginUser;
import com.clinic.util.PageUtil;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.List;

import static java.util.Objects.nonNull;

/**
 * 正在接诊页面接口
 */
@RestController("searchAdmissionList")
@RequestMapping
public class SearchList extends MPJBaseServiceImpl<AdmissionLogMapper, AdmissionLog> {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {

        /**
         * 日志记录 ID
         */
        @NotNull
        private Long id;

        private Integer current;

        private Integer size;

        private String createTime;

        private Integer state;

        private String value;
    }
    @GetMapping("/log/admission/list")
    public Result<Page<AdmissionLog>> search(Param param) {
        List<AdmissionLog> admissionLogs = selectJoinList(AdmissionLog.class, new MPJLambdaWrapper<AdmissionLog>()
                .selectAll(AdmissionLog.class)
                .selectAssociation(Pay.class, AdmissionLog::getPay)
                .eq(AdmissionLog::getUserId, LoginUser.getId())
                .likeRight(nonNull(param.createTime), AdmissionLog::getCreateTime, param.createTime)
                .eq(nonNull(param.state),AdmissionLog::getState, param.state)
                .isNull(nonNull(param.state),Pay::getState)
                .or()
                .eq(AdmissionLog::getUserId, LoginUser.getId())
                .likeRight(nonNull(param.createTime), AdmissionLog::getCreateTime, param.createTime)
                .eq(nonNull(param.state),AdmissionLog::getState, param.state)
                .leftJoin(Pay.class, Pay::getId, AdmissionLog::getPayId)
                .orderByDesc(AdmissionLog::getCreateTime)
        );
        return Result.success(PageUtil.execPage(param.getCurrent(), param.getSize(), admissionLogs));
    }
}