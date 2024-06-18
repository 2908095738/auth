package com.bbs.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName(value = "sku")
public class Sku implements Serializable {

    @ApiModelProperty(value = "唯一标识符")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "SKU编码")
    @TableField(value = "sku_code")
    private String skuCode;

    @ApiModelProperty(value = "展示图片")
    @TableField(value = "pic")
    private String pic;

    @ApiModelProperty(value = "商品Sku属性；json格式")
    @TableField(value = "prod_json")
    private String prodJson;

    @ApiModelProperty(value = "商品价格")
    @TableField(value = "price")
    private Double price;

    @ApiModelProperty(value = "促销价格")
    @TableField(value = "low_price")
    private Double lowPrice;

    @ApiModelProperty(value = "销量")
    @TableField(value = "sale")
    private Integer sale;

    @ApiModelProperty(value = "库存")
    @TableField(value = "stock")
    private Integer stock;

    @ApiModelProperty(value = "预警库存")
    @TableField(value = "low_stock")
    private Integer lowStock;

    @ApiModelProperty(value = "锁定库存")
    @TableField(value = "lock_stock")
    private Integer lockStock;

    @ApiModelProperty(value = "商品id")
    @TableField(value = "product_id")
    private Long productId;

    private static final long serialVersionUID = 1L;
}