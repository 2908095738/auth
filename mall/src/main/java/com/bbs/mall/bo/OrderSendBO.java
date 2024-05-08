package com.bbs.mall.bo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class OrderSendBO {

    @ApiModelProperty(value = "物流id")
    private Long sendId;

    @ApiModelProperty(value = "商品品牌名称")
    private String prodBrand;

    @ApiModelProperty(value = "订单状态：0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单")
    private Integer status;

    @ApiModelProperty(value = "商品图片")
    private String prodPic;

    @ApiModelProperty(value = "商品名称")
    private String prodName;

    @ApiModelProperty(value = "应付金额(实际支付金额)")
    private BigDecimal payPrice;

    @ApiModelProperty(value = "品牌id")
    private Long brandId;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}