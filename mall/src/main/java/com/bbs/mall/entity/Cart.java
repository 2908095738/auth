package com.bbs.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName(value = "cart")
public class Cart implements Serializable {
    @ApiModelProperty(value = "唯一标识符")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "商品品牌名称")
    @TableField(value = "product_brand")
    private String productBrand;

    @ApiModelProperty(value = "商品SN码")
    @TableField(value = "prod_sn")
    private String prodSN;

    @ApiModelProperty(value = "商品SKU编码")
    @TableField(value = "prod_sku_code")
    private String prodSkuCode;

    @ApiModelProperty(value = "购买数量")
    @TableField(value = "quantity")
    private Integer quantity;

    @ApiModelProperty(value = "商品价格")
    @TableField(value = "price")
    private Double price;

    @ApiModelProperty(value = "商品名称")
    @TableField(value = "product_name")
    private String productName;

    @ApiModelProperty(value = "商品图片URL")
    @TableField(value = "product_img")
    private String productImg;

    @ApiModelProperty(value = "商品Sku属性；json格式")
    @TableField(value = "prod_sku_json")
    private String prodSkuJson;

    @ApiModelProperty(value = "是否删除：1.正常；2.逻辑删除")
    @TableField(value = "delete_status")
    private Integer deleteStatus;

    @ApiModelProperty(value = "商品id")
    @TableField(value = "product_id")
    private Long productId;

    @ApiModelProperty(value = "商品skuid")
    @TableField(value = "sku_id")
    private Long skuId;

    @ApiModelProperty(value = "商品分类id")
    @TableField(value = "category_id")
    private Long categoryId;

    @ApiModelProperty(value = "品牌id")
    @TableField(value = "brand_id")
    private Long brandId;

    @ApiModelProperty(value = "用户id")
    @TableField(value = "user_id")
    private Long userId;

    @ApiModelProperty(value = "购物车排序")
    @TableField(value = "sort")
    private Integer sort;

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "create_time")
    private Date createTime;

    @ApiModelProperty(value = "修改时间")
    @TableField(value = "update_time")
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}