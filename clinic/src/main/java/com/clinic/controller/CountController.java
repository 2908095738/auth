package com.clinic.controller;


import com.bbs.Result;
import com.clinic.app.AppStockService;
import com.clinic.dto.param.StatsParam;
import com.clinic.entity.DisinfectionLog;
import com.clinic.entity.SterilizeLog;
import com.clinic.service.AdmissionLogService;
import com.clinic.service.DisinfectionLogService;
import com.clinic.service.SterilizeLogService;
import com.clinic.util.LoginUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Date;

import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;

/**
 * 统计页面
 */
@Slf4j
@RestController("countClinicIndex")
@RequestMapping
public class CountController {

    private final AppStockService appStockService;

    private final AdmissionLogService admissionLogService;

    @Resource
    private SterilizeLogService sterilizeLogService;
    @Resource
    private DisinfectionLogService disinfectionLogService;

    @GetMapping("/count")
    public Result<VO> count() {

        AppStockService.DrugExpiryGroup drugExpiryGroup = appStockService.countAndUpdateDrugExpiryState();

        boolean existCriticalDrug = drugExpiryGroup.getAboutExpires().size() > INTEGER_ZERO;

        boolean isDisinfection = disinfectionLogService.lambdaQuery()
                .eq(DisinfectionLog::getUserId, LoginUser.getId())
                .eq(DisinfectionLog::getCreateTime, new Date())
                .exists();

        boolean isSterilize = sterilizeLogService.lambdaQuery()
                .eq(SterilizeLog::getUserId, LoginUser.getId())
                .eq(SterilizeLog::getCreateTime, new Date())
                .exists();
        return Result.success(new VO());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class VO {

        /**
         * 正在接诊（待接诊数）
         */
        private Long queueNumber;

        /**
         * 正在接诊（待接诊数）
         */
        private Long currentDayTotalReceptionNumber;

        /**
         * 正在接诊（待接诊数）
         */
        private Long currentDayEarnings;

        /**
         * 有无临期药品
         */
        private Boolean existCriticalDrug;

        /**
         * 有无库存不足药品
         */
        private Boolean existUnderStockDrug;

        /**
         * 今日是否消杀
         */
        private Boolean isDisinfection;

        /**
         * 今日是否消毒
         */
        private Boolean isSterilize;
    }


    /**
     * 一段时间接诊病人数量
     */
//    @GetMapping("/patient")
    public Result<Long> countPatient(@Valid StatsParam param){
        Long countPatientNum = admissionLogService.countPatientNum(param);
        return Result.success(countPatientNum);
    }

    /**
     * 一段时间总销售额、数量、利润
     * 总销售额：总金额
     * 数量：病人数量
     * 利润：赚了多少
     */
    @GetMapping("/price")
    public Result<Object> countPrice(@Valid StatsParam param){




        return null;
    }

    @Autowired
    public CountController(AppStockService appStockService, AdmissionLogService admissionLogService) {
        this.appStockService = appStockService;
        this.admissionLogService = admissionLogService;
    }
}
