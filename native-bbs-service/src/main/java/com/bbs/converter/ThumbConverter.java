package com.bbs.converter;

import com.bbs.dto.param.CreateThumbParam;
import com.bbs.entity.Thumb;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ThumbConverter {

    Thumb toEntity(CreateThumbParam param);
}
