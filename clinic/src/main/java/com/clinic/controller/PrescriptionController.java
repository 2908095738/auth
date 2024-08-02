package com.clinic.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.util.PageUtil;
import com.clinic.app.AppPayService;
import com.clinic.app.AppPrescriptionService;
import com.clinic.app.porescription.file.create.CreatePrescriptionFile;
import com.clinic.cache.pay.PayCache;
import com.clinic.cache.unit.UnitCache;
import com.clinic.cache.usage.UsageCache;
import com.clinic.converter.StockConverter;
import com.clinic.dto.PrescriptionAndPayIdVo;
import com.clinic.dto.PrescriptionDto;
import com.clinic.dto.param.SavePrescription;
import com.clinic.dto.param.UpdatePrescription;
import com.clinic.dto.vo.PrescriptionSearchDrugVO;
import com.clinic.entity.Dossier;
import com.clinic.entity.Drug;
import com.clinic.entity.Prescription;
import com.clinic.entity.Stock;
import com.clinic.entity.StockBatch;
import com.clinic.entity.Unit;
import com.clinic.entity.Usage;
import com.clinic.service.AdmissionLogService;
import com.clinic.service.DossierService;
import com.clinic.service.DrugService;
import com.clinic.service.StockBatchService;
import com.clinic.util.LoginUser;
import com.clinic.util.log.LogUtil;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;


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
    @Resource
    private StockBatchService stockBatchService;
    @Resource
    private StockConverter converter;
    @Resource
    private DrugService drugService;

    /**
     * 药品查询
     */
    @GetMapping("/prescription/drug")
    public Result<Page<PrescriptionSearchDrugVO>> searchDrug(
            @RequestParam(required = false) String name,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "50") Integer size
    ) {
        Page<PrescriptionSearchDrugVO> result;
        MPJLambdaWrapper<StockBatch> queryWrapper = new MPJLambdaWrapper<StockBatch>()
                .selectAll(StockBatch.class)
                .leftJoin(Unit.class, Unit::getId, StockBatch::getUnitId, ext -> ext
                        .selectAssociation(Unit.class, StockBatch::getUnit)
                )
                .leftJoin(Unit.class, Unit::getId, StockBatch::getCostUnitId, ext -> ext
                        .selectAssociation(Unit.class, StockBatch::getCostUnit)
                )
                .leftJoin(Unit.class, Unit::getId, StockBatch::getSingleDoseUnit, ext -> ext
                        .selectAssociation(Unit.class, StockBatch::getSingleDoseUnitObj)
                )
                .selectAssociation(Stock.class , StockBatch::getName,ext->ext.result(Stock::getName))
                .leftJoin(Stock.class, "st",Stock::getId, StockBatch::getStockId)
                .eq(StockBatch::getUserId, LoginUser.getId())
        ;

        if(StringUtils.isNotBlank(name)) {
            if (name.matches("[a-zA-Z]+")) {
                // 如果是纯英文，进行拼音或首字母模糊匹配
                queryWrapper.apply("LOWER(CONVERT(st.name USING gbk)) LIKE LOWER(CONVERT({0} USING gbk)) OR LOWER(st.name) LIKE LOWER({0})", "%" + name + "%");
            } else {
                // 否则进行普通 LIKE 查询
                queryWrapper.like("st.name", name);
            }
        }

        List<StockBatch> stockBatches = stockBatchService.selectJoinList(StockBatch.class, queryWrapper);
        Page<StockBatch> stockBatchPage = PageUtil.paginateWithInfo(stockBatches, current, size);
        result = new Page<>(stockBatchPage.getCurrent(), stockBatchPage.getSize(), stockBatchPage.getTotal());
        result.setRecords(
                stockBatchPage.getRecords().stream()
                        .map(stockBatch ->{
                            PrescriptionSearchDrugVO prescriptionSearchDrugVO = converter.toPrescriptionSearchDrugVO(stockBatch);
                            prescriptionSearchDrugVO.setIsStock(true);
                            prescriptionSearchDrugVO.setStockNumberUnit(stockBatch.getUnit().getName());
                            return prescriptionSearchDrugVO;
                        }).collect(Collectors.toList())
        );
        if(stockBatches.isEmpty()||stockBatches.size() != size){
            Page<Drug> search = drugService.search(name, new Page<>(current,size-stockBatches.size()));
            List<PrescriptionSearchDrugVO> records = result.getRecords();
            records.addAll(converter.toPrescriptionSearchDrugVOList(search.getRecords()));
        }
        return Result.success(result);
    }


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
