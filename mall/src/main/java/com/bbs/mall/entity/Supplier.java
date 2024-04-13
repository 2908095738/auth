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
@TableName(value = "supplier")
public class Supplier implements Serializable {
    @ApiModelProperty(value = "唯一标识符")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "供应商名称")
    @TableField(value = "supplier_name")
    private String supplierName;

    @ApiModelProperty(value = "供应商联系方式")
    @TableField(value = "contact_details")
    private String contactDetails;

    @ApiModelProperty(value = "供应商评级")
    @TableField(value = "supplier_rating")
    private String supplierRating;

    @ApiModelProperty(value = "认证和合规性")
    @TableField(value = "certifications")
    private String certifications;

    @ApiModelProperty(value = "税务")
    @TableField(value = "supplier_tax")
    private String supplierTax;

    @ApiModelProperty(value = "供应商状态：1.活跃；2.禁用；3.注销")
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