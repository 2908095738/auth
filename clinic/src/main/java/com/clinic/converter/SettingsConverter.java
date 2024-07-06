package com.clinic.converter;

import com.clinic.dto.param.AddSettingsParam;
import com.clinic.dto.param.UpdateSettingsParam;
import com.clinic.entity.Settings;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SettingsConverter {
    Settings toEntity(AddSettingsParam param);

    Settings toEntity(UpdateSettingsParam param);
}
