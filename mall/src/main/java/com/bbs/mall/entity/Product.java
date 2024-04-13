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
@TableName(value = "product")
public class Product implements Serializable {
    @ApiModelProperty(value = "唯一标识符")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "商品编码")
    @TableField(value = "product_code")
    private Long productCode;

    @ApiModelProperty(value = "商品SN码")
    @TableField(value = "product_sn")
    private Long productSN;

    @ApiModelProperty(value = "商品名称")
    @TableField(value = "product_name")
    private String productName;

    @ApiModelProperty(value = "商品副标题")
    @TableField(value = "sub_title")
    private String subTitle;

    @ApiModelProperty(value = "商品描述")
    @TableField(value = "description")
    private String description;

    @ApiModelProperty(value = "商品图片")
    @TableField(value = "pic")
    private String pic;

    @ApiModelProperty(value = "商品价格")
    @TableField(value = "price")
    private Double price;

    @ApiModelProperty(value = "促销价格")
    @TableField(value = "low_price")
    private Double lowPrice;

    @ApiModelProperty(value = "促销类型：0->没有促销使用原价;1->使用促销价；2->待定；3->使用阶梯价格；4->使用满减价格；5->限时购")
    @TableField(value = "low_type")
    private Integer lowType;

    @ApiModelProperty(value = "商品数量")
    @TableField(value = "stock")
    private Integer stock;

    @ApiModelProperty(value = "商品状态：1.正常；2.删除；3.下架")
    @TableField(value = "status")
    private Integer status;

    @ApiModelProperty(value = "商品分类ID")
    @TableField(value = "category_id")
    private Long categoryId;

    @ApiModelProperty(value = "品牌ID")
    @TableField(value = "brand_id")
    private Long brandId;

    @ApiModelProperty(value = "供应商ID")
    @TableField(value = "supplier_id")
    private Long supplierId;

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "create_time")
    private Date createTime;

    @ApiModelProperty(value = "修改时间")
    @TableField(value = "update_time")
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}