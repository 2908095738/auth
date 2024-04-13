package com.bbs.mall.converter;

import com.bbs.mall.dto.param.ProductParam;
import com.bbs.mall.entity.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductConverter {

    Product toEntity(ProductParam param);
}