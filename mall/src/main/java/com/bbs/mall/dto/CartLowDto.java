package com.bbs.mall.dto;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiOperation("购物车促销商品")
public class CartLowDto {
    @ApiModelProperty(value = "商品id")
    private Long productId;

    @ApiModelProperty(value = "促销商品当前价格")
    private BigDecimal nowPrice;//该实例域暂只在[单品促销]时有用

    @ApiModelProperty("促销活动信息")
    private String lowMsg;

    @ApiModelProperty("促销活动减去的金额")
    private BigDecimal gap;

    @ApiModelProperty("剩余库存-锁定库存")
    private Integer realStock;
}