package com.bbs.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName(value = "prod_attr")
public class ProdAttr implements Serializable {

    @ApiModelProperty(value = "唯一标识符")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "商品属性名称")
    @TableField(value = "name")
    private String name;

    @ApiModelProperty(value = "商品属性值列表，以逗号隔")
    @TableField(value = "value_list")
    private String valueList;

    @ApiModelProperty(value = "商品属性值录入方式：0->手工录入；1->从列表中选取")
    @TableField(value = "value_type")
    private Integer valueType;

    @ApiModelProperty(value = "属性选择类型：0->唯一；1->单选；2->多选")
    @TableField(value = "select_type")
    private Integer selectType;

    @ApiModelProperty(value = "检索类型；0->不需要进行检索；1->关键字检索；2->范围检索")
    @TableField(value = "search_type")
    private Integer searchType;

    @ApiModelProperty(value = "分类筛选样式：1->普通；1->颜色")
    @TableField(value = "filter_type")
    private Integer filterType;

    @ApiModelProperty(value = "相同属性产品是否关联；0->不关联；1->关联")
    @TableField(value = "related_status")
    private Integer relatedStatus;

    @ApiModelProperty(value = "是否支持手动新增；0->不支持；1->支持")
    @TableField(value = "hand_add_status")
    private Integer handAddStatus;

    @ApiModelProperty(value = "属性的类型；0->规格；1->参数")
    @TableField(value = "type")
    private Integer type;

    @ApiModelProperty(value = "商品属性分类id")
    @TableField(value = "prod_attr_cate_id")
    private Long prodAttrCateId;

    @ApiModelProperty(value = "商品id")
    @TableField(value = "prod_id")
    private Long prodId;

    @TableField(value = "sort")
    private Integer sort;

    private static final long serialVersionUID = 1L;
}