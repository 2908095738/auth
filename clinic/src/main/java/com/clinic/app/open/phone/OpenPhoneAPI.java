package com.clinic.app.open.phone;

import com.bbs.Result;
import com.clinic.dto.PrescriptionDto;
import com.clinic.dto.vo.PatientClinicVo;
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
     * 查历史门诊记录
     */
    @GetMapping("/open/patient/admission")
    public Result<List<PatientClinicVo>> searchHistoryAdmission(String openId) {
        return Result.success(admissionLogService.selectByOpenId(openId));
    }

}
