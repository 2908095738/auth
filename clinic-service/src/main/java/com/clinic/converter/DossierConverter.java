package com.clinic.converter;

import com.clinic.dto.param.SaveDossierParam;
import com.clinic.dto.param.UpdateDossierParam;
import com.clinic.entity.Dossier;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DossierConverter {

    Dossier toEntity(SaveDossierParam dossier);

    Dossier toEntity(UpdateDossierParam param);
}
