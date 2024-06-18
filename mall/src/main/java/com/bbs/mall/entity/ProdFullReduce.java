package com.bbs.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName(value = "prod_full_reduce")
public class ProdFullReduce implements Serializable {

    @ApiModelProperty(value = "唯一标识符")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "满足条件价格")
    @TableField(value = "full_price")
    private Double fullPrice;

    @ApiModelProperty(value = "满减价格")
    @TableField(value = "reduce_price")
    private Double reducePrice;

    @ApiModelProperty(value = "商品id")
    @TableField(value = "prod_id")
    private Long prodId;

    private static final long serialVersionUID = 1L;
}