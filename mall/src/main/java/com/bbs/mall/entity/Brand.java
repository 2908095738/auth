package com.bbs.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@ApiModel("品牌实体")
@TableName(value = "brand")
public class Brand implements Serializable {
    @ApiModelProperty(value = "唯一标识符")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "品牌首字母")
    @TableField(value = "first")
    private String first;

    @ApiModelProperty(value = "品牌名称")
    @TableField(value = "name")
    private String name;

    @ApiModelProperty(value = "品牌logo")
    @TableField(value = "logo")
    private String logo;

    @ApiModelProperty(value = "专区大图")
    @TableField(value = "big_pic")
    private String bigPic;

    @ApiModelProperty(value = "品牌故事")
    @TableField(value = "story")
    private String story;

    @ApiModelProperty(value = "简要描述")
    @TableField(value = "brief")
    private String brief;

    @ApiModelProperty(value = "产品数量")
    @TableField(value = "prod_num")
    private Integer prodNum;

    @ApiModelProperty(value = "排序")
    @TableField(value = "sort")
    private Integer sort;

    @ApiModelProperty(value = "品牌状态：0.正常;1.待审核;2.审核中;3.禁用")
    @TableField(value = "status")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "create_time")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(value = "update_time")
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}