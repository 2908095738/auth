package com.bbs.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName(value = "prod_attr_value")
public class ProdAttrValue implements Serializable {
    @ApiModelProperty(value = "唯一标识符")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "手动添加规格或参数的值，参数单值，规格有多个时以逗号隔开")
    @TableField(value = "value")
    private String value;

    @ApiModelProperty(value = "商品属性参数id")
    @TableField(value = "prod_attr_id")
    private Long prodAttrId;

    @ApiModelProperty(value = "商品id")
    @TableField(value = "prod_id")
    private Long prodId;

    private static final long serialVersionUID = 1L;
}