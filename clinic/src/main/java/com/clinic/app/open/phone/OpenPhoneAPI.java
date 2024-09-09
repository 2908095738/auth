package com.clinic.app.open.phone;

import com.bbs.Result;
import com.clinic.dto.PrescriptionDto;
import com.clinic.service.PrescriptionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static com.bbs.util.PhoneUtil.checkPhoneFormat;

/**
 * 手机端开放接口
 */
@RequestMapping
@RestController
public class OpenPhoneAPI {

    @Resource
    private PrescriptionService prescriptionService;

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
    public Result<PrescriptionDto> searchHistoryClinic(String phone) {
        if(StringUtils.isNoneBlank(phone) && checkPhoneFormat(phone)) {

        }
//        return Result.success(prescriptionService.getByAdmissionId(admissionId));
        return Result.success(null);
    }
}
