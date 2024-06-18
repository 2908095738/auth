package com.bbs.mall.converter;

import com.bbs.mall.bo.OrderSendBO;
import com.bbs.mall.dto.CartDto;
import com.bbs.mall.dto.OrderDto;
import com.bbs.mall.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface OrderConverter {

    //TODO
//    @Mappings({
//            @Mapping(target = "id", ignore = true),
//            @Mapping(target = "isMQUpd", ignore = true),//TODO 暂且使用这种方式规避可能的赋值报错
//            @Mapping(target = "createTime", ignore = true),
//            @Mapping(target = "updateTime", ignore = true),
//    })
    //TODO 重构代码后用这种方式
    OrderItem toEntity(CartDto dto);

    OrderDto toDto(OrderSendBO dto);
}