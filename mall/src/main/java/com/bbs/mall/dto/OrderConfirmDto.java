package com.bbs.mall.dto;

import com.bbs.mall.entity.Address;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@ApiOperation("订单确认页")
public class OrderConfirmDto {
    @ApiModelProperty(value = "订单商品列表")
    private List<OrderCartDto> ocDtos;

    @ApiModelProperty("订单促销商品列表")
    private List<CartLowDto> clDtos;

    @ApiModelProperty("收货地址列表")
    private List<AddressDto> addresses;

    @ApiModelProperty("订单商品金额")
    private CalcAmount calcAmount;

    @Data
    public static class CalcAmount {
        @ApiModelProperty("订单总金额")
        private BigDecimal totalAmount;
        @ApiModelProperty("运费金额")
        private BigDecimal sendAmount;
        @ApiModelProperty("促销后金额")
        private BigDecimal lowAmount;
        @ApiModelProperty("应付金额")
        private BigDecimal payAmount;
    }
}