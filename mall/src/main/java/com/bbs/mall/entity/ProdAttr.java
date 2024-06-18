package com.bbs.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("商品属性实体")
@TableName(value = "prod_attr")
public class ProdAttr implements Serializable {

    @ApiModelProperty(value = "唯一标识符")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "商品属性名称")
    @TableField(value = "name")
    private String name;

    @ApiModelProperty(value = "商品可选属性值列表json")
    @TableField(value = "value_list")
    private String valueList;

    @ApiModelProperty(value = "属性选择类型：1.单选;2.多选")
    @TableField(value = "select_type")
    private Integer selectType;

    @ApiModelProperty(value = "属性的类型；1.规格(用户自行输入值);2.参数(列表选择值)")
    @TableField(value = "type")
    private Integer type;

    @TableField(value = "sort")
    private Integer sort;

    private static final long serialVersionUID = 1L;
}