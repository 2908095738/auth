package com.bbs.mall.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProdDetallDto {
    @ApiModelProperty(value = "商品id")
    private Long id;

    @ApiModelProperty(value = "品牌logo")
    private String brandLogo;

    @ApiModelProperty(value = "品牌名称")
    private String brandName;

    @ApiModelProperty(value = "商品价格")
    private BigDecimal price;

    @ApiModelProperty(value = "商品描述")
    private String description;

    @ApiModelProperty(value = "商品属性列表")
    private List<AttrValueDto> values;

    @ApiModelProperty(value = "商品图片列表")
    private List<String> pics;

    @Data
    public static class AttrValueDto {
        @ApiModelProperty(value = "属性名称")
        private String name;

        @ApiModelProperty(value = "属性参数")
        private String value;
    }
}