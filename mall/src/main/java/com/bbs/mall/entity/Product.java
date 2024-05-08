package com.bbs.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel("商品实体")
@TableName(value = "product")
public class Product implements Serializable {
    @ApiModelProperty(value = "唯一标识符")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "商品编码")
    @TableField(value = "prod_code")
    private Long prodCode;

    @ApiModelProperty(value = "商品SN码")
    @TableField(value = "prod_sn")
    private Long prodSN;

    @ApiModelProperty(value = "商品名称")
    @TableField(value = "name")
    private String name;

    @ApiModelProperty(value = "商品描述")
    @TableField(value = "description")
    private String description;

    @ApiModelProperty(value = "商品主图片")
    @TableField(value = "main_pic")
    private String mainPic;

    @ApiModelProperty(value = "商品价格")
    @TableField(value = "price")
    private BigDecimal price;

    @ApiModelProperty(value = "商品成本价格")
    @TableField(value = "cost_price")
    private BigDecimal costPrice;

    @ApiModelProperty(value = "商品数量")
    @TableField(value = "stock")
    private Integer stock;

    @ApiModelProperty(value = "最低库存(低于此值需补货)")
    @TableField(value = "min_stock")
    private Integer minStock;

    @ApiModelProperty(value = "商品状态：0.正常;1.待审核;2.审核通过;3.预售;4.库存预警;5.已售馨;6.下架;7.审核未通过")
    @TableField(value = "status")
    private Integer status;

    @ApiModelProperty(value = "是否需要运费：0.正常;1.不需要")
    @TableField(value = "send_type")
    private Integer sendType;

    @ApiModelProperty(value = "品牌logo")
    @TableField(value = "brand_logo")
    private String brandLogo;

    @ApiModelProperty(value = "品牌名称")
    @TableField(value = "brand_name")
    private String brandName;

    @ApiModelProperty(value = "商品分类ID")
    @TableField(value = "cate_id")
    private Long cateId;

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