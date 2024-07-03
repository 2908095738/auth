package com.clinic.app;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.clinic.dto.GetPayDto;
import com.clinic.dto.PayAndRecordPageDto;
import com.clinic.dto.PayRecordPatientDto;
import com.clinic.dto.param.CreateOrSetPayRecord;
import com.clinic.dto.param.GetPayParam;
import com.clinic.dto.param.PatientPayRecordParam;
import com.clinic.dto.param.UpdatePayById;
import com.clinic.entity.Dossier;
import com.clinic.entity.PayRecord;
import com.clinic.entity.Prescription;

import java.math.BigDecimal;
import java.util.List;



public interface AppPayService {

    Result<GetPayDto> getPay(Long id);

    Result<Page<PayRecordPatientDto>> selectPayPatient(PatientPayRecordParam param);

    Result<Page<PayAndRecordPageDto>> selectPayRecord(GetPayParam param);

    Boolean createPayOther(List<CreateOrSetPayRecord> payOther, Long payId);

    List<PayRecord> updatePayOther(List<CreateOrSetPayRecord> payOther, Long payId);

    boolean updatePayById(UpdatePayById param);

    Long createPayAndPrescriptionRecord(Prescription prescription, Dossier dossier);

    boolean updatePayPrescriptionRecord(Long payId, BigDecimal prescriptionPrice);

}
