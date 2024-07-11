package com.clinic.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.clinic.app.AppPayService;
import com.clinic.app.AppPrescriptionService;
import com.clinic.app.AppStockService;
import com.clinic.cache.pay.PayCache;
import com.clinic.converter.PayConverter;
import com.clinic.dto.GetPayDto;
import com.clinic.dto.PayAndRecordPageDto;
import com.clinic.dto.PayRecordPatientDto;
import com.clinic.dto.PrescriptionDto;
import com.clinic.dto.param.CreateOrSetPayOtherParam;
import com.clinic.dto.param.GetPayParam;
import com.clinic.dto.param.IsPayRecordParam;
import com.clinic.dto.param.NoPayRecordParam;
import com.clinic.dto.param.PatientPayRecordParam;
import com.clinic.dto.param.ReturnPayRecordParam;
import com.clinic.dto.param.UpdatePayById;
import com.clinic.entity.PayRecord;
import com.clinic.service.AdmissionLogService;
import com.clinic.service.PayService;
import com.clinic.util.LogUtil;
import com.clinic.util.LoginUser;
import com.clinic.util.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;


@Slf4j
@RestController
@RequestMapping("/pay")
public class PayController {

    private final AdmissionLogService admissionLogService;
    private final AppPayService service;

    private final PayCache payCache;
    private final PayConverter payConverter;

    private final AppStockService appStockService;

    private final AppPrescriptionService appPrescriptionService;

    private final DataSourceTransactionManager transactionManager;

    private final TransactionDefinition transactionDefinition;

    @Resource
    private PayService payService;
    /**
     * 本次收费-数据回显
     */
    @GetMapping
    public Result<GetPayDto> getPay(@NotNull Long id){
        return service.getPay(id);
    }

    /**
     * 病人收费记录
     */
    @GetMapping("/patient")
    public Result<Page<PayRecordPatientDto>> getPayPatient(PatientPayRecordParam param) throws InterruptedException {
        return payCache.selectPayPatient(param);
    }

    /**
     * 收费列表-已收费
     */
    @GetMapping("/all/is")
    public Result<Page<PayAndRecordPageDto>> getPay(
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String val,
            @RequestParam(required = false) Long startDate,
            @RequestParam(required = false) Long endDate
    ){
        GetPayParam param = new GetPayParam(new Page<>(current, size), val, startDate, endDate);
        param.setState(INTEGER_ONE);
        List<PayAndRecordPageDto> data = payService.selectPayAndRecordDto(param);
        return Result.success(PageUtil.execPage(current, size, data));
    }

    /**
     * 收费列表-未收费
     */
    @GetMapping("/all/no")
    public Result<Page<PayAndRecordPageDto>> getNoPay(
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String val,
            @RequestParam(required = false) Long startDate,
            @RequestParam(required = false) Long endDate
    ){
        GetPayParam param = new GetPayParam(new Page<>(current, size), val, startDate, endDate);
        param.setState(INTEGER_ZERO);
        List<PayAndRecordPageDto> data = payService.selectPayAndRecordDto(param);
        return Result.success(PageUtil.execPage(current, size, data));
    }

    /**
     * 收费列表-退费
     */
    @GetMapping("/all/return")
    public Result<Page<PayAndRecordPageDto>> getPay(ReturnPayRecordParam param){
        GetPayParam p = payConverter.toParam(param);
        return service.selectPayRecord(p);
    }


    /**
     * 其他收费-项目创建
     */
    @PutMapping("/other")
    public Result<Boolean> createPayOther(@RequestBody @Valid CreateOrSetPayOtherParam param){
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            if(!service.createPayOther(param.getPayOther(), param.getPayId())) throw new RuntimeException();
            LogUtil.Operation.record("收费", LoginUser.get().getName()+"其他收费项创建：收费id="+param.getPayId(), Level.INFO);
            transactionManager.commit(transaction);
            return Result.success(true);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return Result.failed();
        }
    }


    /**
     * 其他收费-项目修改
     */
    @PostMapping("/other")
    public Result<List<PayRecord>> updatePayOther(@RequestBody @Valid CreateOrSetPayOtherParam param){
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            List<PayRecord> payRecordList = service.updatePayOther(param.getPayOther(), param.getPayId());
            LogUtil.Operation.record("收费",LoginUser.get().getName()+"其他收费项修改：收费id="+param.getPayId(), Level.INFO);
            transactionManager.commit(transaction);
            return Result.success(payRecordList);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return Result.failedNull();
        }
    }


    /**
     * 本次收费-修改收费状态和收费方式
     */
    @PostMapping
    public Result<Boolean> updatePayById(@RequestBody UpdatePayById param){
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            //收费
            if(!payCache.updatePayById(param))throw new RuntimeException();
            //根据支付id,查出处方数据
            PrescriptionDto prescriptionDto = appPrescriptionService.getByPayId(param.getId());
            //根据处方数据，扣库存
            if(!appStockService.updateNum(prescriptionDto))throw new RuntimeException();
            //修改门诊日志状态
            if (!admissionLogService.updateEndState(param.getAdmissionId()))throw new RuntimeException();
            LogUtil.Operation.record("收费",LoginUser.get().getName()+"本次收费-修改收费状态和收费方式：收费id="+param.getId()+", 处方id="+prescriptionDto.getId(), Level.INFO);
            transactionManager.commit(transaction);
            return Result.success(true);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return Result.failed();
        }
    }

    @Autowired
    public PayController(AdmissionLogService admissionLogService, AppPayService service, PayCache payCache, PayConverter payConverter, AppStockService appStockService, AppPrescriptionService appPrescriptionService, DataSourceTransactionManager transactionManager, TransactionDefinition transactionDefinition) {
        this.admissionLogService = admissionLogService;
        this.service = service;
        this.payCache = payCache;
        this.payConverter = payConverter;
        this.appStockService = appStockService;
        this.appPrescriptionService = appPrescriptionService;
        this.transactionManager = transactionManager;
        this.transactionDefinition = transactionDefinition;
    }
}
