package com.clinic.app.log.operation;

import cn.hutool.core.date.DateUtil;
import com.bbs.Result;
import com.clinic.entity.OperationLog;
import com.clinic.entity.Patient;
import com.clinic.service.OperationLogService;
import com.clinic.service.PatientService;
import com.clinic.util.LoginUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.*;
import java.util.stream.Collectors;

import static com.clinic.util.log.ServiceLogEnums.*;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;

@RestController("searchOperationLog")
@RequestMapping
public class Search {

    @Resource
    private OperationLogService operationLogService;
    @Resource
    private PatientService patientService;

    @GetMapping("/log/operation/list")
    public Result<List<OperationLog>> search(
            @RequestParam(required = false) Long startDateLong,
            @RequestParam(required = false) Long endDateLong
    ) {
        Date startDate = getStartDate(startDateLong);
        Date endDate = getEndDate(endDateLong);
        List<OperationLog> logs = operationLogService.lambdaQuery()
                .ge(OperationLog::getCreateTime, DateUtil.beginOfDay(startDate))
                .lt(OperationLog::getCreateTime, DateUtil.endOfDay(endDate))
                .eq(OperationLog::getUserId, LoginUser.getId())
                // 只筛选部分，对诊所医生有用的操作日志类型
                .in(OperationLog::getServiceCode, Arrays.asList(
                        STOCK_ADD.getServiceCode(),
                        RETAIL.getServiceCode(),
                        ADMISSION.getServiceCode(),
                        PAY.getServiceCode(),
                        PRESCRIPTION_ADD.getServiceCode(),
                        DIAGNOSIS_PROOF_ADD.getServiceCode(),
                        DISINFECTION_ADD.getServiceCode(),
                        STERILIZE_ADD.getServiceCode(),
                        DOSSIER_ADD.getServiceCode()
                ))
                .orderByDesc(OperationLog::getCreateTime)
                .list();
        fillPatient(logs);
        return Result.success(logs);
    }

    private void fillPatient(List<OperationLog> logs) {
        if(logs.size() > INTEGER_ZERO) {
            // 填充病人信息
            Date now = new Date();
            List<OperationLog> needFillPatientLogs = new ArrayList<>();
            Set<Long> needFillPatientLogIds = logs.stream().peek(log -> {
                Date createTime = log.getCreateTime();
                log.setIsCurrentDay(DateUtil.isSameDay(now, createTime));
                log.setCreateYMD(DateUtil.format(createTime, "yyyy/MM/dd"));
                log.setCreateHMS(DateUtil.format(createTime, "HH:mm:ss"));

            }).filter(log -> {
                Integer serviceCode = log.getServiceCode();
                // 零售药品
                return (
                        RETAIL.getServiceCode().equals(serviceCode) ||                      // 零售药品
                                ADMISSION.getServiceCode().equals(serviceCode) ||           // 接诊
                                PAY.getServiceCode().equals(serviceCode) ||                 // 支付
                                PRESCRIPTION_ADD.getServiceCode().equals(serviceCode) ||    // 开具处方
                                DIAGNOSIS_PROOF_ADD.getServiceCode().equals(serviceCode) || // 诊断证明
                                DOSSIER_ADD.getServiceCode().equals(serviceCode)            // 新增病例
                );
            }).peek(needFillPatientLogs::add).map(OperationLog::getPatientId).collect(Collectors.toSet());

            if(needFillPatientLogIds.size() > INTEGER_ZERO) {
                Map<Long, Patient> patientIdMap = patientService.listByIds(needFillPatientLogIds)
                        .stream().collect(Collectors.toMap(Patient::getId, patient -> patient));
                needFillPatientLogs.forEach(log -> {
                    Patient patient = patientIdMap.get(log.getPatientId());
                    if(nonNull(patient.getSex())) patient.setSexStr(Objects.equals(patient.getSex(), INTEGER_ONE) ? "男" : "女");
                    if(nonNull(patient.getAge())) patient.setAgeStr(patient.getAge() + "岁");
                    log.setPatient(patientIdMap.get(log.getPatientId()));
                });
            }
        }
    }

    private Date getStartDate(Long startDateLong) {
        Date startDate;
        if(nonNull(startDateLong)) {
            startDate = new Date(startDateLong);
        } else {
            startDate = new Date();
        }
        return startDate;
    }

    private Date getEndDate(Long endDateLong) {
        Date endDate;
        if(nonNull(endDateLong)) {
            endDate = new Date(endDateLong);
        } else {
            endDate = new Date();
        }
        return endDate;
    }
}
