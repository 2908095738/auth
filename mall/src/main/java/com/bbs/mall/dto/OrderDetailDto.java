package com.bbs.mall.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class OrderDetailDto {
    private Long id;

    @ApiModelProperty(value = "商品品牌名称")
    private String prodBrand;

    @ApiModelProperty(value = "订单商品列表")
    private List<OrderItemDto> items;

    @ApiModelProperty(value = "商品促销活动减去的金额")
    private BigDecimal lowGap;

    @ApiModelProperty(value = "应付金额(实际支付金额)")
    private BigDecimal payAmount;

    @ApiModelProperty(value = "订单编号")
    @TableField(value = "order_sn")
    private String orderSN;

    @ApiModelProperty(value = "支付方式：0->未支付；1->支付宝；2->微信")
    @TableField(value = "payType")
    private Integer payType;

    @ApiModelProperty(value = "支付时间")
    @TableField(value = "pay_time")
    private Date payTime;

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "create_time")
    private Date createTime;

    @ApiModelProperty(value = "物流公司(配送方式)")
    @TableField(value = "send_company")
    private String sendCompany;

    @ApiModelProperty(value = "物流单号")
    private String sendSN;

    @ApiModelProperty(value = "收货人姓名")
    @TableField(value = "shou_name")
    private String shouName;

    @ApiModelProperty(value = "收货人电话")
    @TableField(value = "shou_phone")
    private String shouPhone;

    @ApiModelProperty(value = "收货地址")
    private String address;

    @Data
    public static class OrderItemDto {
        @ApiModelProperty(value = "商品图片")
        private String prodPic;

        @ApiModelProperty(value = "商品名称")
        private String prodName;

        @ApiModelProperty(value = "商品价格")
        @TableField(value = "prod_price")
        private BigDecimal prodPrice;

        @ApiModelProperty(value = "购买数量")
        private Integer prodQuantity;

        @ApiModelProperty(value = "商品Sku属性；json格式")
        @TableField(value = "prod_sku_json")
        private String prodSkuJson;
    }
}