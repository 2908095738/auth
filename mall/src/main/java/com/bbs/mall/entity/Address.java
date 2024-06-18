package com.bbs.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName(value = "address")
public class Address implements Serializable {
    @ApiModelProperty(value = "唯一标识符")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "收货人名称")
    @TableField(value = "name")
    private String name;

    @ApiModelProperty(value = "联系方式")
    @TableField(value = "phone")
    private String phone;

    @ApiModelProperty(value = "邮政编码")
    @TableField(value = "post_code")
    private String postCode;

    @ApiModelProperty(value = "省份/直辖市")
    @TableField(value = "province")
    private String province;

    @ApiModelProperty(value = "城市")
    @TableField(value = "city")
    private String city;

    @ApiModelProperty(value = "区")
    @TableField(value = "region")
    private String region;

    @ApiModelProperty(value = "详细地址(街道)")
    @TableField(value = "detail")
    private String detail;

    @ApiModelProperty(value = "是否为默认地址；0：正常；1：默认")
    @TableField(value = "one_status")
    private String oneStatus;

    @ApiModelProperty(value = "用户id")
    @TableField(value = "user_id")
    private Long userId;

    private static final long serialVersionUID = 1L;
}