package com.clinic.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bbs.Result;
import com.clinic.app.AppPayService;
import com.clinic.app.AppPrescriptionService;
import com.clinic.app.porescription.file.create.CreatePrescriptionFile;
import com.clinic.cache.pay.PayCache;
import com.clinic.cache.unit.UnitCache;
import com.clinic.cache.usage.UsageCache;
import com.clinic.dto.PrescriptionAndPayIdVo;
import com.clinic.dto.PrescriptionDto;
import com.clinic.dto.param.SavePrescription;
import com.clinic.dto.param.UpdatePrescription;
import com.clinic.entity.*;
import com.clinic.service.*;
import com.clinic.util.LoginUser;
import com.clinic.util.log.LogUtil;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.*;

import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;


/**
 * 处方
 */
@RestController
public class PrescriptionController {


    @Resource
    private AppPrescriptionService service;
    @Resource
    private AppPayService appPayService;
    @Resource
    private PayCache payCache;
    @Resource
    private DossierService dossierService;
    @Resource
    private AdmissionLogService admissionLogService;
    @Resource
    private DataSourceTransactionManager transactionManager;
    @Resource
    private TransactionDefinition transactionDefinition;
    @Resource
    private UsageCache usageCache;
    @Resource
    private UnitCache unitCache;

    /**
     * 添加处方
     * @param param 处方信息
     * @return PrescriptionAndPayIdVo
     */
    @PutMapping("/prescription")
    public Result<PrescriptionAndPayIdVo> add(@RequestBody @Valid SavePrescription param){
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            Prescription prescription = service.save(param);
            Dossier dossier = dossierService.getDossierByPrescriptionId(prescription.getId());
            Long payId = payCache.createPayAndPrescriptionRecord(prescription,dossier);
            admissionLogService.update(param.getAdmissionId(),prescription.getId(),payId);
            LogUtil.Operation.addPrescription(param.getPatientId(), prescription.getId(), "{}添加处方并创建收费记录：处方id={}, 支付id={}", LoginUser.get().getName(), prescription.getId(), payId);
            transactionManager.commit(transaction);
            return Result.success(new PrescriptionAndPayIdVo(prescription.getId(), payId));
        } catch (RuntimeException e) {
            transactionManager.rollback(transaction);
            e.printStackTrace();
            return Result.failedNull();
        }
    }

    /**
     * 修改处方
     * @param param 处方信息
     * @return Long
     */
    @PostMapping("/prescription")
    public Result<Boolean> update(@RequestBody @Valid UpdatePrescription param){
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            if(service.update(param))if(!appPayService.updatePayPrescriptionRecord(param.getPayId(), param.getPrice()) )throw new RuntimeException();
            LogUtil.Operation.updatePrescription(param.getPatientId(), param.getPayId(), "{}修改处方和处方收费记录：处方id={}, 支付id={}", LoginUser.get().getName(), param.getId(), param.getPayId());
            transactionManager.commit(transaction);
            return Result.success();
        } catch (RuntimeException e) {
            transactionManager.rollback(transaction);
            e.printStackTrace();
            return Result.failed();
        }
    }


    /**
     * 处方查询
     * @param dossierId 病人ID
     * @param patientId 病例ID
     * @param current 页码
     * @param size 条数
     * @return null
     */
    @GetMapping("/prescription")
    public Result<IPage<PrescriptionDto>> selectOr(Long dossierId, Long patientId, Integer current, Integer size){
        return service.selectOr(dossierId, patientId, current, size);
    }

    @Resource
    private CreatePrescriptionFile createPrescriptionFile;

    /**
     * 处方下载
     * @param id 处方ID
     * @param templateIndex 处方模板下标 API:/prescription/template/name/list
     * @see PrescriptionController
     * @throws Exception 下载异常
     */
    @GetMapping("/prescription/file")
    public void getFile(@NotNull Long id, @NotNull Integer templateIndex) throws Exception {
        LogUtil.Operation.downloadPrescription(id, "{}下载处方：处方id={}, 模板id={}", LoginUser.get().getName(), id, templateIndex);
        createPrescriptionFile.generation(id, templateIndex);
    }

    /**
     * 获取模板名称列表
     * @return 模板名称列表
     */
    @GetMapping("/prescription/template/name/list")
    public Result<List<String>> getTemplates() {
        return Result.success(createPrescriptionFile.getTemplates());
    }

    @GetMapping("/prescription/usage/list")
    public Result<List<Usage>> searchUsage(String name) {
        return Result.success(usageCache.search(name));
    }

    @GetMapping("/prescription/unit/list")
    public Result<List<Unit>> searchUnit(String name) {
        return Result.success(unitCache.search(name));
    }
}
