package com.bbs.converter;

import com.bbs.dto.param.CreateNewParam;
import com.bbs.entity.News;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NewsConverter {
    News toEntity(CreateNewParam param);
}
