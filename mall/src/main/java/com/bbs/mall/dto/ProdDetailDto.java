package com.bbs.mall.dto;

import com.bbs.mall.entity.*;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

import java.util.List;

@Data
@ApiOperation("商品详情")
public class ProdDetailDto {

    @ApiModelProperty(value = "商品信息")
    private Product product;

    @ApiModelProperty(value = "商品品牌")
    private Brand brand;

    @ApiModelProperty(value = "商品属性信息(key)")
    private List<ProdAttr> attrs;

    @ApiModelProperty(value = "商品属性值(value)")
    private List<ProdAttrValue> attrValues;

    @ApiModelProperty("商品sku信息")
    private List<Sku> skus;

    @ApiModelProperty("促销规则")
    private List<String> lowRuleJson;
}
