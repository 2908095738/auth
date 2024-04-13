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
@TableName(value = "brand")
public class Brand implements Serializable {
    @ApiModelProperty(value = "唯一标识符")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "品牌首字母")
    @TableField(value = "brand_first")
    private String brandFirst;

    @ApiModelProperty(value = "品牌名称")
    @TableField(value = "brand_name")
    private String brandName;

    @ApiModelProperty(value = "品牌logo")
    @TableField(value = "brand_logo")
    private String brandLogo;

    @ApiModelProperty(value = "专区大图")
    @TableField(value = "big_pic")
    private String bigPic;

    @ApiModelProperty(value = "品牌故事")
    @TableField(value = "brand_story")
    private String brandStory;

    @ApiModelProperty(value = "简要描述")
    @TableField(value = "brief")
    private String brief;

    @ApiModelProperty(value = "产品数量")
    @TableField(value = "product_count")
    private Integer productCount;

    @ApiModelProperty(value = "排序")
    @TableField(value = "sort")
    private Integer sort;

    @ApiModelProperty(value = "品牌状态：1.活跃；2.禁用")
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