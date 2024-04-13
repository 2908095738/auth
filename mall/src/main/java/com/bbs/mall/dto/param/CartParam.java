package com.bbs.mall.dto.param;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CartParam {
    @ApiModelProperty(value = "商品品牌名称")
    private String productBrand;

    @ApiModelProperty(value = "商品SN码")
    private String prodSN;

    @ApiModelProperty(value = "商品SKU编码")
    private String prodSkuCode;

    @ApiModelProperty(value = "购买数量")
    private Integer quantity;

    @ApiModelProperty(value = "商品价格")
    private Double price;

    @ApiModelProperty(value = "商品名称")
    private String productName;

    @ApiModelProperty(value = "商品图片URL")
    private String productImg;

    @ApiModelProperty(value = "商品Sku属性；json格式")
    private String prodSkuJson;

    @ApiModelProperty(value = "商品id")
    private Long productId;

    @ApiModelProperty(value = "商品skuid")
    private Long skuId;

    @ApiModelProperty(value = "商品分类id")
    private Long categoryId;

    @ApiModelProperty(value = "品牌id")
    private Long brandId;
}