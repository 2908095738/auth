package com.clinic.app.open.phone;

import com.bbs.Result;
import com.clinic.dto.PrescriptionDto;
import com.clinic.entity.AdmissionLog;
import com.clinic.service.AdmissionLogService;
import com.clinic.service.PrescriptionService;
import com.clinic.service.SettingsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 手机端开放接口
 */
@RequestMapping
@RestController
public class OpenPhoneAPI {

    @Resource
    private PrescriptionService prescriptionService;

    @Resource
    private AdmissionLogService admissionLogService;
    @Resource
    private SettingsService settingsService;

    /**
     * 查询处方
     */
    @GetMapping("/open/prescription")
    public Result<PrescriptionDto> searchPrescription(Long admissionId){
        return Result.success(prescriptionService.getByAdmissionId(admissionId));
    }

    /**
     * 查去过哪些诊所
     */
    @GetMapping("/open/patient/clinic")
    public Result<Set<String>> searchHistoryClinic(Long patientId) {
        return Result.success(new HashSet<>(settingsService.getClinic(patientId)));
    }


    /**
     * 查去就诊记录
     */
    @GetMapping("/open/patient/admission")
    public Result<List<AdmissionLog>> searchHistoryAdmission(Long patientId) {
        return Result.success(admissionLogService.selectByPatientId(patientId));
    }

    /**
     * 查去全部处方
     */
    @GetMapping("/open/patient/prescription")
    public Result<List<PrescriptionDto>> searchHistoryPrescription(Long patientId) {
        return Result.success(prescriptionService.select(patientId));
    }


}
