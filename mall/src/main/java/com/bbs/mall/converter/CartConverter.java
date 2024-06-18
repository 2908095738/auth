package com.bbs.mall.converter;

import com.bbs.mall.dto.CartDto;
import com.bbs.mall.dto.OrderCartDto;
import com.bbs.mall.dto.param.CartParam;
import com.bbs.mall.dto.param.UpdCartParam;
import com.bbs.mall.entity.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface CartConverter {
    CartDto toDto(CartParam param);


    //TODO
//    @Mappings({
//            @Mapping(target = "isMQUpd", ignore = true),//TODO 暂且使用这种方式规避可能的赋值报错
//            @Mapping(target = "createTime", ignore = true),
//            @Mapping(target = "updateTime", ignore = true),
//    })
    OrderCartDto toDto(CartDto dto);

    Cart toEntity(CartParam param);

    Cart toEntity(UpdCartParam param);

    Cart toEntity(CartDto dto);
}