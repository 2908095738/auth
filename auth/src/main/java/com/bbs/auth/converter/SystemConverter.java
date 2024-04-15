package com.bbs.auth.converter;

import com.bbs.auth.app.system.CreateSystem;
import com.bbs.auth.app.system.UpdateAdmin;
import com.bbs.auth.entity.System;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SystemConverter {

    System toEntity(CreateSystem.Param param);

    System toEntity(UpdateAdmin.Param param);
}
