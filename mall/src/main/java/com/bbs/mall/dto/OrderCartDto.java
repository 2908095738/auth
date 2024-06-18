package com.bbs.mall.dto;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiOperation("订单商品")
public class OrderCartDto {
    @ApiModelProperty(value = "商品品牌名称")
    private String productBrand;

    @ApiModelProperty(value = "购买数量")
    private Integer quantity;

    @ApiModelProperty(value = "商品价格")
    private BigDecimal price;

    @ApiModelProperty(value = "商品名称")
    private String productName;

    @ApiModelProperty(value = "商品图片URL")
    private String productImg;

    @ApiModelProperty(value = "商品Sku属性；json格式")
    private String prodJson;

    @ApiModelProperty(value = "商品id")
    private Long productId;

    @ApiModelProperty(value = "商品skuid")
    private Long skuId;

    @ApiModelProperty(value = "购物车商品排序")
    private Integer sort;
}