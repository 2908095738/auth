package com.bbs.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName(value = "prod_attr_category")
public class ProdAttrCate implements Serializable {

    @ApiModelProperty(value = "唯一标识符")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "商品属性分类名称")
    @TableField(value = "name")
    private String name;

    @ApiModelProperty(value = "属性数量")
    @TableField(value = "attr_count")
    private Integer attrCount;

    @ApiModelProperty(value = "参数数量")
    @TableField(value = "param_count")
    private Integer paramCount;

    @ApiModelProperty(value = "父商品属性分类ID")
    @TableField(value = "parent_id")
    private Long parentId;

    @ApiModelProperty(value = "分类ID")
    @TableField(value = "cate_id")
    private Long cateId;

    private static final long serialVersionUID = 1L;
}