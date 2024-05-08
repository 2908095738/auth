package com.bbs.mall.bo;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

@Data
@ApiOperation("订单提示信息")
public class ItemTipBO {
    @ApiModelProperty(value = "商品图片")
    private String itemPic;

    @ApiModelProperty(value = "商品名称")
    private String itemName;

    @ApiModelProperty(value = "订单编号")
    private String orderSN;
}