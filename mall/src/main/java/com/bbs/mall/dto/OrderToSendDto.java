package com.bbs.mall.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class OrderToSendDto {

    @ApiModelProperty(value = "订单id")
    private Long orderId;

    @ApiModelProperty(value = "订单状态：0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单")
    private Integer orderStatus;

    @ApiModelProperty(value = "同订单商品列表")
    private List<BrandDto> brandDtos;

    @ApiModelProperty(value = "应付金额(实际支付金额)")
    private BigDecimal payAmount;

    @ApiModelProperty(value = "订单商品数量(复数则只显示商品图片)")
    private Integer orderNum;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @Data
    public static class BrandDto {
        @ApiModelProperty(value = "商品品牌名称")
        private String prodBrand;

        @ApiModelProperty(value = "品牌id")
        private Long brandId;

        @ApiModelProperty(value = "商品图片")
        private String prodPic;

        @ApiModelProperty(value = "商品名称")
        private String prodName;
    }
}