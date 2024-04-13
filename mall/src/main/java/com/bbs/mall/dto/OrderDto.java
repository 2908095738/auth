package com.bbs.mall.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class OrderDto {

    private Long id;

    @ApiModelProperty(value = "订单编号")
    private String orderSN;

    @ApiModelProperty(value = "应付金额(实际支付金额)")
    private BigDecimal payAmount;

    @ApiModelProperty(value = "订单状态：0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单")
    private Integer status;

    @ApiModelProperty(value = "商品品牌名称")
    private String prodBrand;

    @ApiModelProperty(value = "商品图片列表(订单同一品牌的商品数量是复数时，只显示商品图片)")
    private List<String> prodPics;

    @ApiModelProperty(value = "商品名称")
    private String prodName;

    @ApiModelProperty(value = "商品数量(订单同一个品牌的商品数量)")
    private Integer prodNum;

    @ApiModelProperty(value = "品牌id")
    private Long brandId;

    @ApiModelProperty(value = "物流单号")
    private String sendSN;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}