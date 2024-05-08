package com.bbs.mall.dto.param;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

import java.util.Date;

@Data
@ApiOperation("订单创建参数")
public class ProductParam {
    @ApiModelProperty(value = "唯一标识符")
    private Long id;

    @ApiModelProperty(value = "商品编码")
    private Long productCode;

    @ApiModelProperty(value = "商品SN码")
    private Long productSN;

    @ApiModelProperty(value = "商品名称")
    private String productName;

    @ApiModelProperty(value = "商品描述")
    private String description;

    @ApiModelProperty(value = "图片URL")
    private String imageUrl;

    @ApiModelProperty(value = "商品价格")
    private Double price;

    @ApiModelProperty(value = "商品数量")
    private Integer stock;

    @ApiModelProperty(value = "商品状态：1.正常；2.删除；3.下架")
    private Integer status;

    @ApiModelProperty(value = "商品分类ID")
    private Long categoryId;

    @ApiModelProperty(value = "品牌ID")
    private Long brandId;

    @ApiModelProperty(value = "供应商ID")
    private Long supplierId;

    @ApiModelProperty(value = "商品属性参数id")
    private Long prodAtrrId;
}