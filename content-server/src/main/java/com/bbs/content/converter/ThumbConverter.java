package com.bbs.content.converter;

import com.bbs.content.dto.param.CreateThumbParam;
import com.bbs.content.entity.Thumb;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ThumbConverter {

    Thumb toEntity(CreateThumbParam param);
}
