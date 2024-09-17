package com.clinic.app;

import com.bbs.Result;
import com.bbs.api.auth.UserAPI;
import com.clinic.entity.*;
import com.clinic.service.*;
import com.clinic.util.ApiPermissionManagement;
import com.clinic.util.LoginUser;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.clinic.util.ApiPermissionManagement.checkApiPermissionManagementCode;


@RestController
@RequestMapping
public class UserDelete {

    @Resource
    private DiagnosisProofService diagnosisProofService;

    @Resource
    private DossierService dossierService;

    @Resource
    private DossierPrescriptionService dossierPrescriptionService;
    @Resource
    private PrescriptionService prescriptionService;
    @Resource
    private PrescriptionDrugService prescriptionDrugService;
    @Resource
    private AdmissionLogService admissionLogService;
    @Resource
    private OperationLogService operationLogService;
    @Resource
    private PatientService patientService;
    @Resource
    private PayService payService;
    @Resource
    private PayRecordService payRecordService;
    @Resource
    private RetailDrugRecordService retailDrugRecordService;
    @Resource
    private RetailRecordService retailRecordService;
    @Resource
    private SettingsService settingsService;
    @Resource
    private SterilizeLogService sterilizeLogService;
    @Resource
    private StockService stockService;
    @Resource
    private StockBatchService stockBatchService;
    @Resource
    private StockInService stockInService;
    @Resource
    private StockInDrugService stockInDrugService;
    @Resource
    private StockOutService stockOutService;
    @Resource
    private UserAPI userAPI;



    /**
     * 注销用户
     */
    @Transactional
    @DeleteMapping("/user")
    public Result<Boolean> delete(@RequestBody ApiPermissionManagement.Param param) {
        Long loginUserId = LoginUser.getId();
        checkApiPermissionManagementCode(param);
        diagnosisProofService.lambdaUpdate().eq(DiagnosisProof::getUserId, loginUserId).remove();
        List<Dossier> dossiers = dossierService.lambdaQuery().eq(Dossier::getUserId, loginUserId).list();
        Set<Long> dossierIds = dossiers.stream().map(Dossier::getId).collect(Collectors.toSet());
        dossierService.removeBatchByIds(dossierIds);    // 删病历
        dossierPrescriptionService.lambdaUpdate().in(DossierPrescription::getDossierId, dossierIds).remove();   // 删病历处方关联表
        List<Prescription> prescriptions = prescriptionService.lambdaQuery().eq(Prescription::getCreator, loginUserId).list();
        Set<Long> prescriptionIds = prescriptions.stream().map(Prescription::getId).collect(Collectors.toSet());
        prescriptionDrugService.lambdaUpdate().in(PrescriptionDrug::getPrescriptionId, prescriptionIds).remove();   //删处方药品表
        prescriptionService.removeBatchByIds(prescriptionIds);  // 删处方
        admissionLogService.lambdaUpdate().eq(AdmissionLog::getUserId, loginUserId).remove();   // 删接诊日志
        operationLogService.lambdaUpdate().eq(OperationLog::getUserId, loginUserId).remove();   // 删操作日志
        patientService.lambdaUpdate().eq(Patient::getUserId, loginUserId).remove(); //删病人信息
        payService.lambdaUpdate().eq(Pay::getCreator, loginUserId).remove(); //删病人支付记录
        payRecordService.lambdaUpdate().eq(PayRecord::getCreator, loginUserId).remove();    //删除病人其他收费记录

        List<RetailRecord> retailRecords = retailRecordService.lambdaQuery().eq(RetailRecord::getUserId, loginUserId).list();
        Set<Long> retailRecordIds = retailRecords.stream().map(RetailRecord::getId).collect(Collectors.toSet());
        retailDrugRecordService.lambdaUpdate().in(RetailDrugRecord::getRetailId, retailRecordIds).remove();
        retailRecordService.removeBatchByIds(retailRecordIds);
        settingsService.lambdaUpdate().eq(Settings::getUserId, loginUserId).remove();

        sterilizeLogService.lambdaUpdate().eq(SterilizeLog::getUserId, loginUserId).remove();

        List<Stock> stocks = stockService.lambdaQuery().eq(Stock::getUserId, loginUserId).list();
        Set<Long> stockIds = stocks.stream().map(Stock::getUserId).collect(Collectors.toSet());
        stockBatchService.lambdaUpdate().in(StockBatch::getStockId, stockIds).remove();
        stockService.removeBatchByIds(stockIds);
        stockInService.lambdaUpdate().eq(StockIn::getUserId, loginUserId).remove();
        stockInDrugService.lambdaUpdate().eq(StockInDrug::getUserId, loginUserId).remove();
        stockOutService.lambdaUpdate().eq(StockOut::getUserId, loginUserId).remove();
        userAPI.removeUserById(loginUserId);
        return Result.success();
    }
}
