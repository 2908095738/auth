package com.bbs.mall.dto.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class OrderParam {
    @ApiModelProperty("收货地址id")
    private Long addrId;

    @ApiModelProperty(value = "支付方式：0->未支付；1->支付宝；2->微信")
    private Integer payType;

    /**
     * Q：为什么id分为两个？
     * A：商品添加到购物车时，优先添加到缓存。但添加到缓存后，为避免用户立刻删掉购物车商品，所以入库采用延时队列的形式。
     * 由此导致入库前，缓存中的数据是没有id的。
     */

    @ApiModelProperty("商品id列表")
    private List<Long> prodIds;

    @ApiModelProperty("购物车商品id列表")
    private List<Long> cartIds;
}