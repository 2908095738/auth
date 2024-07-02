package com.clinic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.clinic.entity.PrescriptionDrug;

import java.util.List;

/**
 *
 */
public interface PrescriptionDrugService extends IService<PrescriptionDrug> {

    List<PrescriptionDrug> searchDrug(Long prescriptionId);




}
