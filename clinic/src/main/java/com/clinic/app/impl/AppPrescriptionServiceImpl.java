package com.clinic.app.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bbs.Result;
import com.clinic.app.AppPrescriptionService;
import com.clinic.converter.PrescriptionConverter;
import com.clinic.dto.PrescriptionDrugDto;
import com.clinic.dto.PrescriptionDto;
import com.clinic.dto.param.SavePrescription;
import com.clinic.dto.param.UpdatePrescription;
import com.clinic.entity.DossierPrescription;
import com.clinic.entity.Prescription;
import com.clinic.entity.PrescriptionDrug;
import com.clinic.entity.StockBatch;
import com.clinic.service.DossierPrescriptionService;
import com.clinic.service.PrescriptionDrugService;
import com.clinic.service.PrescriptionService;
import com.clinic.util.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Service
public class AppPrescriptionServiceImpl implements AppPrescriptionService {

    private PrescriptionService prescriptionService;

    private DossierPrescriptionService dossierPrescriptionService;

    private PrescriptionDrugService prescriptionDrugService;

    private PrescriptionConverter prescriptionConverter;

    public Prescription save(SavePrescription param, Long dossierId) {
        return initAndCreate(param, dossierId);
    }


    public boolean update(UpdatePrescription param) {
        return updatePrescription(param);
    }


    private boolean updatePrescription(UpdatePrescription param) {
        List<PrescriptionDrug> drugList = prescriptionConverter.toEntityDrug(param.getDrugList());
        Prescription prescription = prescriptionConverter.toEntity(param);
        if(!prescriptionService.updateById(prescription))throw new RuntimeException();
        if(!updateDrug(drugList,param.getId()))throw new RuntimeException();
        return true;
    }

    private boolean updateDrug(List<PrescriptionDrug> drugList, Long id) {
        prescriptionDrugService.lambdaUpdate().eq(PrescriptionDrug::getPrescriptionId,id).remove();
        drugList.forEach(drug-> drug.setPrescriptionId(id));
        return prescriptionDrugService.saveBatch(drugList);
    }

    private Prescription initAndCreate(SavePrescription param, Long dossierId) {
        List<PrescriptionDrug> drugList = prescriptionConverter.toEntityDrugList(param.getDrugList());
        Prescription prescription = prescriptionConverter.toEntity(param);
        fillPrescription(prescription);
        if(prescriptionService.save(prescription)) {
            Long id = prescription.getId();
            drugList.forEach(drug-> drug.setPrescriptionId(id));
            if(saveDrug(drugList) && saveDossierPrescription(id, dossierId)) {
                return prescription;
            }
        }
        return null;
    }

    private void fillPrescription(Prescription prescription) {
        LocalDate tomorrow = LocalDateTime.now().plusDays(NumberUtils.INTEGER_ONE).toLocalDate();
        Date tomo =Date.from(tomorrow.atStartOfDay(ZoneId.systemDefault()).toInstant());

        prescription.setCreator(LoginUser.getId());
        prescription.setUpdator(prescription.getCreator());
        prescription.setExpiryDate(NumberUtils.INTEGER_ONE);
        prescription.setExpirationDate(tomo);
    }

    private Boolean saveDrug(List<PrescriptionDrug> drugList) { return prescriptionDrugService.saveBatch(drugList); }

    private Boolean saveDossierPrescription(Long id, Long dossierId) {
        return dossierPrescriptionService.save(new DossierPrescription(dossierId, id));
    }


    public Result<IPage<PrescriptionDto>> selectOr(Long dossierId, Long patientId, Integer current, Integer size) {
        IPage<PrescriptionDto> result = prescriptionService.selectPage(LoginUser.getId(), dossierId, patientId, current, size);
        if (CollectionUtil.isNotEmpty(result.getRecords())){
            result.getRecords().forEach(prescription-> prescription.setDrugList(getDrugList(prescription.getId())));
        }
        return Result.success(result);
    }


    public List<PrescriptionDto> selectByIds(Long id, List<Long> dossierIds){
        List<PrescriptionDto> prescriptionList = prescriptionService.select(id, dossierIds);
        if (CollectionUtil.isNotEmpty(prescriptionList)){
            prescriptionList.forEach(prescription-> prescription.setDrugList(getDrugList(prescription.getId())));
        }
        return prescriptionList;
    }

    @Override
    public List<PrescriptionDrugDto> getDrugList(Long id) {
        List<PrescriptionDrug> prescriptionDrugs = prescriptionDrugService.searchDrug(id);
        if (CollectionUtils.isNotEmpty(prescriptionDrugs)) {
            return  prescriptionConverter.toDto(prescriptionDrugs.stream().peek(o2 -> {
                if(CollectionUtils.isNotEmpty(o2.getBatchList())){
                    StockBatch stockBatch = o2.getBatchList().get(0);
                    o2.setNumber(stockBatch.getNumber());
                }
            }).collect(Collectors.toList()));
        }
        return  prescriptionConverter.toDto(prescriptionDrugs);
    }

    public PrescriptionDto getByPayId(Long payId) {
        return prescriptionService.getByPayId(payId);
    }


    @Resource
    public void setPrescriptionService(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }
    @Resource
    public void setDossierPrescriptionService(DossierPrescriptionService dossierPrescriptionService) {
        this.dossierPrescriptionService = dossierPrescriptionService;
    }
    @Resource
    public void setPrescriptionDrugService(PrescriptionDrugService prescriptionDrugService) {
        this.prescriptionDrugService = prescriptionDrugService;
    }
    @Resource
    public void setPrescriptionConverter(PrescriptionConverter prescriptionConverter) {
        this.prescriptionConverter = prescriptionConverter;
    }

}
