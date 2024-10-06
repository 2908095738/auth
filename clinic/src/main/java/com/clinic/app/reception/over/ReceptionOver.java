package com.clinic.app.reception.over;

import cn.hutool.core.collection.CollUtil;
import com.bbs.Result;
import com.clinic.app.AppPayService;
import com.clinic.app.AppPrescriptionService;
import com.clinic.app.impl.AppPrescriptionServiceImpl;
import com.clinic.cache.pay.PayCache;
import com.clinic.converter.PrescriptionConverter;
import com.clinic.entity.*;
import com.clinic.service.*;
import com.clinic.util.LoginUser;
import com.clinic.util.log.LogUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.hutool.core.util.ObjectUtil.isNotNull;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;

/**
 * 接诊完成
 * 含创建病例、创建处方、创建支付记录、更新接诊记录（不含收费）
 */
@RestController
@RequestMapping
public class ReceptionOver {

    @Resource
    private DossierService dossierService;
    @Resource
    private AdmissionLogService admissionLogService;
    @Resource
    private DataSourceTransactionManager transactionManager;
    @Resource
    private TransactionDefinition transactionDefinition;
    @Resource
    private PrescriptionConverter prescriptionConverter;
    @Resource
    private PrescriptionService prescriptionService;
    @Resource
    private PrescriptionDrugService prescriptionDrugService;
    @Resource
    private DossierPrescriptionService dossierPrescriptionService;
    @Resource
    private PayCache payCache;
    @Resource
    private AppPrescriptionService appPrescriptionService;
    @Resource
    private AppPayService appPayService;

    @PutMapping("/reception")
    public Result<Boolean> create(@Valid @RequestBody Params param) {
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        if(CollUtil.isEmpty(param.getPrescription().getDrugList())){
            return Result.failed("请检查您的开药处方是否为空");
        }
        try {
            Long admissionIds = param.getAdmissionIds();
            AdmissionLog admissionLog = searchAdmissionLog(admissionIds);//查询接诊记录
            Dossier dossier = saveOrUpdateDossier(admissionLog, param);//保存病例
            Long prescriptionId = admissionLog.getPrescriptionId();
            if(nonNull(prescriptionId)) { // 更新处方
                PrescriptionParam prescriptionParam = param.getPrescription();
                updatePrescription(prescriptionId, prescriptionParam);
                updatePrescriptionDrug(prescriptionId, prescriptionParam);
                updatePayRecord(admissionLog, prescriptionParam);
                updateAdmissionLog(admissionLog, prescriptionId, dossier);
                recordLog(admissionLog);
            } else { // 保存处方
                if(isNeedSavePrescription(param)){  //是否需要保存处方
                    com.clinic.entity.Prescription prescription = savePrescription(param, admissionLog, dossier);   // 保存处方
                    Long payId = createPayRecord(prescription,dossier); //创建支付记录，并返回支付ID（用户后续更新支付状态）
                    updateAdmissionLog(param, prescription, payId, dossier); //更新接诊记录
                    LogUtil.Operation.addPrescription(admissionLog.getPatientId(), prescription.getId(), "{}添加处方并创建收费记录：处方id={}, 支付id={}", LoginUser.get().getName(), prescription.getId(), payId);
                }
            }
            commit(transaction);
            return Result.success();
        } catch (IllegalArgumentException e) {
            rollback(transaction);
            return Result.success(400, e.getMessage());
        } catch (Exception e) {
            rollback(transaction);
            e.printStackTrace();
            return Result.failed();
        }
    }

    private void recordLog(AdmissionLog admissionLog) {
        LogUtil.Operation.updatePrescription(admissionLog.getPatientId(), admissionLog.getPayId(), "{}修改处方和处方收费记录：处方id={}, 支付id={}",
                LoginUser.get().getName(), admissionLog.getPrescriptionId(), admissionLog.getPayId());
    }

    private void updateAdmissionLog(AdmissionLog admissionLog, Long prescriptionId, Dossier dossier) {
        admissionLogService.update(admissionLog.getId(), prescriptionId, admissionLog.getPayId(), admissionLog.getDossierId(), dossier.getDiagnosis());
    }

    private void updatePayRecord(AdmissionLog admissionLog, PrescriptionParam prescriptionParam) {
        appPayService.updatePayPrescriptionRecord(admissionLog.getPayId(), prescriptionParam.getPrice());
    }

    private void updatePrescription(Long prescriptionId, PrescriptionParam prescriptionParam) {
        Prescription prescription = prescriptionConverter.toEntity(prescriptionParam);
        prescription.setId(prescriptionId);
        prescriptionService.updateById(prescription);
    }

    private void updatePrescriptionDrug(Long prescriptionId, PrescriptionParam prescription) {
        List<PrescriptionDrug> drugList = prescriptionConverter.toEntityDrug(prescription.getDrugList());
        appPrescriptionService.updateDrug(drugList, prescriptionId);
    }

    private void updateAdmissionLog(Params param, Prescription prescription, Long payId, Dossier dossier) {
        admissionLogService.update(param.getAdmissionIds(), prescription.getId(), payId, dossier.getId(), dossier.getDiagnosis());
    }

    private Long createPayRecord(Prescription prescription, Dossier dossier) {
        return payCache.createPayAndPrescriptionRecord(prescription,dossier);
    }

    /**
     * 保存处方相关信息（处方主表、处方药品表、病例处方关联表）
     * @TableName prescription 处方主表
     * @TableName prescription_drug 处方药品表
     * @TableName dossier_prescription 病例处方关联表
     */
    private Prescription savePrescription(Params param, AdmissionLog admissionLog, Dossier dossier) {
        List<PrescriptionDrug> drugList = prescriptionConverter.toEntityDrug(param.getPrescription().getDrugList());
        Prescription prescription = converterPrescription(param, admissionLog);
        savePrescription(prescription); // 保存处方信息
        fillPrescriptionIdToDrugList(drugList, prescription);// 填充处方 ID 到【处方药品】信息中
        saveDrugList(drugList); // 保存处方的药品信息
        saveDossierPrescription(prescription, dossier);// 保存病例与处方的关联
        return prescription;
    }

    private void saveDossierPrescription(Prescription prescription, Dossier dossier) {
        dossierPrescriptionService.save(new DossierPrescription(prescription, dossier));
    }

    private void saveDrugList(List<PrescriptionDrug> drugList) {
        drugList.forEach(drug -> {
            checkArgument(nonNull(drug.getName()), "药品名称不能为空");
            checkArgument(nonNull(drug.getSingleDose())  && drug.getSingleDose() > INTEGER_ZERO, "药品单次剂量不能为空");
            checkArgument(isNotEmpty(drug.getFrequency()), "频次不能为空");
            checkArgument(isNotEmpty(drug.getSingleDoseUnit()), "药品单次剂量单位不能为空");
            checkArgument(nonNull(drug.getQuantity()) && drug.getQuantity() > INTEGER_ZERO, "药品单次剂量不能为空");
            checkArgument(isNotEmpty(drug.getQuantityUnit()), "药品单位不能为空");
            checkArgument(nonNull(drug.getPeriod())  && drug.getPeriod() > INTEGER_ZERO, "天数不能为空");
            checkArgument(isNotEmpty(drug.getPeriodUnit()), "天数单位不能为空");
        });
        prescriptionDrugService.saveBatch(drugList);
    }

    private void fillPrescriptionIdToDrugList(List<PrescriptionDrug> drugList, Prescription prescription) {
        Long id = prescription.getId();
        drugList.forEach(drug-> drug.setPrescriptionId(id));
    }

    private void savePrescription(Prescription prescription) {
        prescriptionService.save(prescription);
    }

    private Prescription converterPrescription(Params param, AdmissionLog admissionLog) {
        Prescription prescription = new Prescription();
        AppPrescriptionServiceImpl.fillPrescription(prescription, param.getPrescription().getPrice(), param.getPrescription().getRemark(), admissionLog.getPatientId());
        return prescription;
    }

    private Boolean isNeedSavePrescription(Params param) {
        return isNotNull(param.getPrescription()) && CollectionUtils.isNotEmpty(param.getPrescription().getDrugList());
    }

    private void commit(TransactionStatus transaction) {
        transactionManager.commit(transaction);
    }

    private void rollback(TransactionStatus transaction) {
        transactionManager.rollback(transaction);
    }

    private AdmissionLog searchAdmissionLog(Long admissionId) {
        return admissionLogService.getById(admissionId);
    }

    private Dossier saveOrUpdateDossier(AdmissionLog admissionLog, Params param) {
        return dossierService.createOrUpdateDossier(admissionLog, param.getDossier());
    }
}