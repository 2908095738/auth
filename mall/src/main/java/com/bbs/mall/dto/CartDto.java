package com.bbs.mall.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class CartDto {
    private Long id;

    @ApiModelProperty(value = "商品品牌名称")
    private String prodBrand;

    @ApiModelProperty(value = "商品SN码")
    private String prodSN;

    @ApiModelProperty(value = "商品SKU编码")
    private String prodSkuCode;

    @ApiModelProperty(value = "购买数量")
    private Integer quantity;

    @ApiModelProperty(value = "商品价格")
    private BigDecimal price;

    @ApiModelProperty(value = "商品名称")
    private String prodName;

    @ApiModelProperty(value = "商品图片URL")
    private String prodPic;

    @ApiModelProperty(value = "商品Sku属性；json格式")
    private String prodSkuJson;

    @ApiModelProperty(value = "商品id")
    private Long prodId;

    @ApiModelProperty(value = "商品skuid")
    private Long skuId;

    @ApiModelProperty(value = "商品分类id")
    private Long prodCateId;

    @ApiModelProperty(value = "品牌id")
    private Long brandId;

    //TODO 未来摘掉这个实例域，因为专用于redis->rabbitmq，考虑转json进缓存前，对jObj加键值对。
    @ApiModelProperty(value = "MQ修改状态(规避修改接口优先操作缓存时，一遍遍发MQ延时消息)：0：正常；1：修改中")
    private Boolean isMQUpd;

    /**
     * -1：用于缓存，购物车商品顺序，改用创建时间排序。
     * Cause：存入缓存的购物车商品有上限。例如刚好存到上限。但用户偏偏热衷删中间的商品，导致后面存入缓存的购物车商品的sort一直是上限值，以至于无法正确排序。
     */
    @ApiModelProperty(value = "购物车商品排序")
    private Integer sort;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "修改时间")
    private Date updateTime;
}