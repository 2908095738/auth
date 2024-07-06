package com.clinic.converter;

import com.clinic.app.porescription.file.create.CreatePrescriptionFile;
import com.clinic.dto.PrescriptionDrugDto;
import com.clinic.dto.param.SavePrescription;
import com.clinic.dto.param.SavePrescriptionDrug;
import com.clinic.dto.param.UpdatePrescription;
import com.clinic.dto.param.UpdatePrescriptionDrug;
import com.clinic.entity.Prescription;
import com.clinic.entity.PrescriptionDrug;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PrescriptionConverter {

    Prescription toEntity(UpdatePrescription param);

    List<PrescriptionDrug> toEntityDrug(List<UpdatePrescriptionDrug> drugList);

    List<PrescriptionDrugDto> toDto(List<PrescriptionDrug> list);

    List<PrescriptionDrug> toEntityDrugList(List<SavePrescriptionDrug> drugList);
    Prescription toEntity(SavePrescription param);

    CreatePrescriptionFile.Drug toFileModel(PrescriptionDrugDto dto);
}
