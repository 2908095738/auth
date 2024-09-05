package com.clinic.controller;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bbs.Result;
import com.clinic.app.AppPayService;
import com.clinic.app.AppPrescriptionService;
import com.clinic.app.porescription.file.create.CreatePrescriptionFile;
import com.clinic.cache.pay.PayCache;
import com.clinic.cache.unit.UnitCache;
import com.clinic.cache.usage.UsageCache;
import com.clinic.dto.PrescriptionDto;
import com.clinic.dto.param.SaveOrUpdatePrescription;
import com.clinic.entity.AdmissionLog;
import com.clinic.entity.Dossier;
import com.clinic.entity.Prescription;
import com.clinic.entity.Unit;
import com.clinic.entity.Usage;
import com.clinic.service.AdmissionLogService;
import com.clinic.service.DossierService;
import com.clinic.util.LoginUser;
import com.clinic.util.log.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

import static cn.hutool.core.util.ObjectUtil.isEmpty;


/**
 * 处方
 */
@Slf4j
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
    public Result<Boolean> add(@RequestBody @Valid SaveOrUpdatePrescription param){
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            AdmissionLog admissionLog = admissionLogService.getById(param.getAdmissionLogId());
            Dossier dossier = dossierService.createOrUpdateDossier(admissionLog.getId(), admissionLog.getPatientId(),param.getDossier());
            if (isEmpty(param.getPrescriptionId())) {
                if(CollUtil.isNotEmpty(param.getDrugList())){
                    Prescription prescription = service.save(param,admissionLog.getPatientId(), dossier.getId());
                    Long payId = payCache.createPayAndPrescriptionRecord(prescription,dossier);
                    admissionLogService.update(param.getAdmissionLogId(),prescription.getId(), payId, dossier.getId(), dossier.getDiagnosis());
                    LogUtil.Operation.addPrescription(admissionLog.getPatientId(), prescription.getId(), "{}添加处方并创建收费记录：处方id={}, 支付id={}", LoginUser.get().getName(), prescription.getId(), payId);
                }
            }else {
                if(service.update(param))
                    if(!appPayService.updatePayPrescriptionRecord(param.getPayId(), param.getPrice()) )throw new RuntimeException();
                admissionLogService.update(param.getAdmissionLogId(), param.getPrescriptionId(), param.getPayId(), dossier.getId(), dossier.getDiagnosis());
                LogUtil.Operation.updatePrescription(admissionLog.getPatientId(), param.getPayId(), "{}修改处方和处方收费记录：处方id={}, 支付id={}",
                LoginUser.get().getName(), admissionLog.getPrescriptionId(), param.getPayId());
            }
            transactionManager.commit(transaction);
            return Result.success(true);
        } catch (RuntimeException e) {
            transactionManager.rollback(transaction);
            e.printStackTrace();
            return Result.failedNull();
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
