package com.clinic.controller;


import com.bbs.Result;
import com.clinic.app.AppStockService;
import com.clinic.dto.param.StatsParam;
import com.clinic.service.AdmissionLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 统计页面
 */
@Slf4j
@RestController
@RequestMapping("/count")
public class CountController {

    private final AppStockService appStockService;

    private final AdmissionLogService admissionLogService;


    /**
     * 一段时间接诊病人数量
     */
    @GetMapping("/patient")
    public Result<Long> countPatient(@Valid StatsParam param){
        Long countPatientNum = admissionLogService.countPatientNum(param);
        return Result.success(countPatientNum);
    }

    /**
     * 一段时间总销售额、数量、利润
     */
    @GetMapping("/price")
    public Result<Object> countPrice(@Valid StatsParam param){




        return null;
    }

    /**
     * 药品库存不足与快过期列表
     */
    @GetMapping("/stock")
    public Result<List<Map<String,Object>>> stockDeficiencyAlert() {
        return appStockService.stockDeficiencyAlert();
    }

    @Autowired
    public CountController(AppStockService appStockService, AdmissionLogService admissionLogService) {
        this.appStockService = appStockService;
        this.admissionLogService = admissionLogService;
    }
}
