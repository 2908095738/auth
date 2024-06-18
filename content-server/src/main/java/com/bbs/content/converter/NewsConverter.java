package com.bbs.content.converter;

import com.bbs.content.dto.param.CreateNewParam;
import com.bbs.content.entity.News;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NewsConverter {
    News toEntity(CreateNewParam param);
}
