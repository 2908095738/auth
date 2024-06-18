package com.bbs.mall.dto;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

@Data
@ApiOperation("收货地址")
public class AddressDto {
    @ApiModelProperty(value = "唯一标识符")
    private Long id;

    @ApiModelProperty(value = "收货人名称")
    private String name;

    @ApiModelProperty(value = "联系方式")
    private String phone;

    @ApiModelProperty(value = "邮政编码")
    private String postCode;

    @ApiModelProperty(value = "省份/直辖市")
    private String province;

    @ApiModelProperty(value = "城市")
    private String city;

    @ApiModelProperty(value = "区")
    private String region;

    @ApiModelProperty(value = "详细地址(街道)")
    private String detail;

    @ApiModelProperty(value = "是否为默认地址；0：正常；1：默认")
    private String oneStatus;
}