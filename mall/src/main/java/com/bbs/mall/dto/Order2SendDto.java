package com.bbs.mall.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class Order2SendDto {
    @ApiModelProperty(value = "订单商品列表")
    private List<ItemTipDto> items;

    @ApiModelProperty(value = "应付金额(实际支付金额)")
    private BigDecimal payAmount;

    @ApiModelProperty(value = "订单商品数量(复数则只显示商品图片)")
    private Integer itemNum;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @Data
    public static class ItemTipDto {
        @ApiModelProperty(value = "商品图片")
        private String itemPic;

        @ApiModelProperty(value = "商品名称")
        private String itemName;
    }
}