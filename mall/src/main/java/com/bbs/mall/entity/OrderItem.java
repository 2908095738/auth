package com.bbs.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单商品
 */
@Data
@Accessors(chain = true)
@TableName(value = "order_item")
public class OrderItem implements Serializable {

    @ApiModelProperty(value = "唯一标识符")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "订单id")
    @TableField(value = "order_id")
    private Long orderId;

    @ApiModelProperty(value = "订单SN码")
    @TableField(value = "order_sn")
    private String orderSN;

    @ApiModelProperty(value = "物流单号")
    @TableField(value = "send_sn")
    private String sendSN;

    @ApiModelProperty(value = "商品品牌名称")
    @TableField(value = "prod_brand")
    private String prodBrand;

    @ApiModelProperty(value = "商品SN码")
    @TableField(value = "prod_sn")
    private String prodSN;

    @ApiModelProperty(value = "商品SKU编码")
    @TableField(value = "prod_sku_code")
    private String prodSkuCode;

    @ApiModelProperty(value = "商品图片")
    @TableField(value = "prod_pic")
    private String prodPic;

    @ApiModelProperty(value = "商品名称")
    @TableField(value = "prod_name")
    private String prodName;

    @ApiModelProperty(value = "商品Sku属性；json格式")
    @TableField(value = "prod_sku_json")
    private String prodSkuJson;

    @ApiModelProperty(value = "购买数量")
    @TableField(value = "prod_quantity")
    private Integer prodQuantity;

    @ApiModelProperty(value = "商品价格")
    @TableField(value = "prod_price")
    private BigDecimal prodPrice;

    @ApiModelProperty(value = "商品促销活动信息")
    @TableField(value = "low_msg")
    private String lowMsg;

    @ApiModelProperty(value = "商品促销活动减去的金额")
    @TableField(value = "low_gap")
    private BigDecimal lowGap;

    @ApiModelProperty(value = "实际支付金额")
    @TableField(value = "pay_price")
    private BigDecimal payPrice;

    @ApiModelProperty(value = "商品id")
    @TableField(value = "prod_id")
    private Long prodId;

    @ApiModelProperty(value = "商品skuid")
    @TableField(value = "prod_sku_id")
    private Long prodSkuId;

    @ApiModelProperty(value = "商品分类id")
    @TableField(value = "prod_cate_id")
    private Long prodCateId;

    @ApiModelProperty(value = "品牌id")
    @TableField(value = "brand_id")
    private Long brandId;

    @ApiModelProperty(value = "物流id")
    @TableField(value = "send_id")
    private Long sendId;

    private static final long serialVersionUID = 1L;
}