package com.bbs.mall.dto.param;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class UpdCartParam {
    private Long id;

    @ApiModelProperty(value = "商品价格")
    private BigDecimal price;

    @ApiModelProperty(value = "商品图片URL")
    private String productImg;

    @ApiModelProperty(value = "商品id")
    private Long productId;

    @ApiModelProperty(value = "商品skuid")
    private Long skuId;

    @ApiModelProperty(value = "购物车排序")
    @TableField(value = "sort")
    private Integer sort;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "修改时间")
    private Date updateTime;
}