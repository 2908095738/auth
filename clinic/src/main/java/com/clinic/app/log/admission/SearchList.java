package com.clinic.app.log.admission;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.util.PageUtil;
import com.bbs.util.StringUtil;
import com.clinic.entity.AdmissionLog;
import com.clinic.entity.Pay;
import com.clinic.mapper.AdmissionLogMapper;
import com.clinic.util.LoginUser;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;

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

        private String endTime;

        private Integer state;

        private String value;
        private Long patientId;
    }
    @GetMapping("/log/admission/list")
    public Result<Page<AdmissionLog>> search(Param param) {
        List<AdmissionLog> admissionLogs = searchList(param);
        if(ObjUtil.isEmpty(param.current)&&ObjUtil.isEmpty(param.size)){
            return Result.success(new Page<AdmissionLog>().setRecords(admissionLogs));
        }
        Page<AdmissionLog> admissionLogPage = PageUtil.paginateWithInfo(admissionLogs, param.getCurrent(), param.getSize());
        return Result.success(admissionLogPage);
    }

    public List<AdmissionLog> searchList(Param param) {
        MPJLambdaWrapper<AdmissionLog> admissionLogMPJLambdaWrapper = new MPJLambdaWrapper<AdmissionLog>()
                .selectAll(AdmissionLog.class)
                .selectAssociation(Pay.class, AdmissionLog::getPay)
                .eq(AdmissionLog::getUserId, LoginUser.getId())
                .eq(nonNull(param.state), AdmissionLog::getState, param.state)
                .eq(AdmissionLog::getUserId, LoginUser.getId())
                .eq(nonNull(param.patientId),AdmissionLog::getPatientId, nonNull(param.patientId) ? param.patientId : null)
                .eq(StringUtils.isNotBlank(param.createTime) && StringUtils.isBlank(param.endTime), AdmissionLog::getCreateTime, nonNull(param.createTime) ? param.createTime : null)
                .and(nonNull(param.createTime) && nonNull(param.endTime), wrapper -> wrapper
                        .ge(AdmissionLog::getCreateTime, nonNull(param.createTime) ? DateUtil.beginOfDay(new Date(param.createTime)) : null)
                        .lt(AdmissionLog::getCreateTime, nonNull(param.endTime) ? DateUtil.endOfDay(new Date(param.endTime)) : null)
                )
                .and(nonNull(param.state), w -> w.isNull(nonNull(param.state), Pay::getState).or().eq(nonNull(param.state), AdmissionLog::getState, param.state))
                .leftJoin(Pay.class, Pay::getId, AdmissionLog::getPayId)
                .orderByDesc(AdmissionLog::getCreateTime);
        if(StrUtil.isNotBlank(param.value)){
            if(StringUtil.isNumeric(param.value)){
                admissionLogMPJLambdaWrapper.eq(AdmissionLog::getPhone, Long.valueOf(param.value));
            }else{
                admissionLogMPJLambdaWrapper.likeRight(AdmissionLog::getName, param.value);
            }
        }
        return selectJoinList(AdmissionLog.class, admissionLogMPJLambdaWrapper);
    }
}