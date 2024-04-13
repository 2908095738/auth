package com.bbs.mall.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ProductDto {
    @ApiModelProperty(value = "商品id")
    private Long id;

    @ApiModelProperty(value = "商品名称")
    private String productName;

    @ApiModelProperty(value = "商品副标题")
    private String subTitle;

    @ApiModelProperty(value = "商品图片")
    private String pic;

    @ApiModelProperty(value = "商品价格")
    private Double price;

    @ApiModelProperty(value = "商品数量")
    private Integer stock;

    @ApiModelProperty(value = "商品属性名称")
    private String pAtrrName;

    @ApiModelProperty(value = "商品SKU图片")
    private String skuPic;

    @ApiModelProperty(value = "商品SKU价格")
    private Double skuPrice;

    @ApiModelProperty(value = "商品SKU库存")
    private Integer skuStock;

    @ApiModelProperty(value = "商品SKU编码")
    private String skuCode;

    @ApiModelProperty(value = "商品SKUid")
    private Long skuId;

    @ApiModelProperty(value = "商品属性参数id")
    private Long prodAtrrId;

    @ApiModelProperty(value = "商品属性分类id")
    private Long prodAttrCateId;
}