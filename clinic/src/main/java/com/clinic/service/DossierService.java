package com.clinic.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.Result;
import com.bbs.exception.BusinessException;
import com.clinic.dto.param.SaveDossierParam;
import com.clinic.dto.param.UpdateDossierParam;
import com.clinic.entity.Dossier;

/**
 * 病历
 */
public interface DossierService extends IService<Dossier> {

    Dossier createDossier(Long admissionID, Long patientId, SaveDossierParam param) throws BusinessException;

    Result<Page<Dossier>> select(Long userId, String id, Integer current, Integer size);

    Dossier getDossierByPrescriptionId(Long prescriptionId);

    Dossier updateDossier(UpdateDossierParam dossier);
}
