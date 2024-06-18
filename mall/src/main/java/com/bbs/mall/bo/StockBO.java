package com.bbs.mall.bo;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

@Data
@ApiOperation("库存BO")
public class StockBO {

    @ApiModelProperty(value = "商品SKUid")
    private Long prodSkuId;

    @ApiModelProperty(value = "购买数量")
    private Integer prodQuantity;
}