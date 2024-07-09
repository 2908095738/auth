package com.clinic.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.clinic.app.AppPayService;
import com.clinic.app.AppPrescriptionService;
import com.clinic.app.porescription.file.create.CreatePrescriptionFile;
import com.clinic.cache.pay.PayCache;
import com.clinic.cache.prescription.CureCache;
import com.clinic.cache.unit.UnitCache;
import com.clinic.cache.usage.UsageCache;
import com.clinic.dto.PrescriptionAndPayIdVo;
import com.clinic.dto.PrescriptionDto;
import com.clinic.dto.param.SavePrescription;
import com.clinic.dto.param.UpdatePrescription;
import com.clinic.dto.vo.PrescriptionSearchDrugVO;
import com.clinic.entity.Dossier;
import com.clinic.entity.Prescription;
import com.clinic.entity.Unit;
import com.clinic.entity.Usage;
import com.clinic.service.AdmissionLogService;
import com.clinic.service.DossierService;
import com.clinic.util.LogUtil;
import com.clinic.util.LoginUser;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;



/**
 * 处方
 */
@RestController
@RequestMapping("prescription")
public class PrescriptionController {


    private final AppPrescriptionService service;

    private final AppPayService appPayService;

    private final PayCache payCache;

    private final DossierService dossierService;

    private final AdmissionLogService admissionLogService;

    private final DataSourceTransactionManager transactionManager;

    private final TransactionDefinition transactionDefinition;

    private final CureCache cache;

    private final UsageCache usageCache;

    private final UnitCache unitCache;

    /**
     * 药品查询
     */
    @GetMapping("/drug")
    public Result<Page<PrescriptionSearchDrugVO>> searchDrug(String name) throws InterruptedException {
        return cache.search(name);
    }

    /**
     * 添加处方
     * @param param 处方信息
     * @return PrescriptionAndPayIdVo
     */
    @PutMapping
    public Result<PrescriptionAndPayIdVo> add(@RequestBody @Valid SavePrescription param){
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            Prescription prescription = service.save(param);
            Dossier dossier = dossierService.getDossierByPrescriptionId(prescription.getId());
            Long payId = payCache.createPayAndPrescriptionRecord(prescription,dossier);
            admissionLogService.update(param.getAdmissionId(),prescription.getId(),payId);
            LogUtil.Operation.record("处方", LoginUser.get().getName()+"添加处方并创建收费记录：处方id="+prescription.getId()+", 支付id="+payId, Level.INFO);
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
    @PostMapping
    public Result<Boolean> update(@RequestBody @Valid UpdatePrescription param){
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            if(service.update(param))if(!appPayService.updatePayPrescriptionRecord(param.getPayId(), param.getPrice()) )throw new RuntimeException();
            LogUtil.Operation.record("处方",LoginUser.get().getName()+"修改处方和处方收费记录：处方id="+param.getId()+", 支付id="+param.getPayId(), Level.INFO);
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
    @GetMapping
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
    @GetMapping("/file")
    public void getFile(@NotNull Long id, @NotNull Integer templateIndex) throws Exception {
        LogUtil.Operation.record("处方",LoginUser.get().getName()+"下载处方：处方id="+id+", 模板id="+templateIndex, Level.INFO);
        createPrescriptionFile.generation(id, templateIndex);
    }

    /**
     * 获取模板名称列表
     * @return 模板名称列表
     */
    @GetMapping("/template/name/list")
    public Result<List<String>> getTemplates() {
        return Result.success(createPrescriptionFile.getTemplates());
    }

    @GetMapping("/usage/list")
    public Result<List<Usage>> searchUsage(String name) {
        return Result.success(usageCache.search(name));
    }

    @GetMapping("/unit/list")
    public Result<List<Unit>> searchUnit(String name) {
        return Result.success(unitCache.search(name));
    }


    @Autowired
    public PrescriptionController(AppPrescriptionService service, AppPayService appPayService, PayCache payCache, DossierService dossierService, AdmissionLogService admissionLogService, DataSourceTransactionManager transactionManager, TransactionDefinition transactionDefinition, CureCache cache, UsageCache usageCache, UnitCache unitCache) {
        this.service = service;
        this.appPayService = appPayService;
        this.payCache = payCache;
        this.dossierService = dossierService;
        this.admissionLogService = admissionLogService;
        this.transactionManager = transactionManager;
        this.transactionDefinition = transactionDefinition;
        this.cache = cache;
        this.usageCache = usageCache;
        this.unitCache = unitCache;
    }
}
