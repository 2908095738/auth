package com.bbs.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName(value = "prod_ladder")
public class ProdLadder implements Serializable {

    @ApiModelProperty(value = "唯一标识符")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "满足的商品数量")
    @TableField(value = "count")
    private Integer count;

    @ApiModelProperty(value = "折扣")
    @TableField(value = "discount")
    private Double discount;

    @ApiModelProperty(value = "折后价格")
    @TableField(value = "price")
    private Double price;

    @ApiModelProperty(value = "商品id")
    @TableField(value = "prod_id")
    private Long prodId;

    private static final long serialVersionUID = 1L;
}