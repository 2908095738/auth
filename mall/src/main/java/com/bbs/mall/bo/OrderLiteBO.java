package com.bbs.mall.bo;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiOperation("订单列表简要信息")
public class OrderLiteBO {

    @ApiModelProperty(value = "订单编号")
    private String orderSN;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "应付金额(实际支付金额)")
    private BigDecimal payAmount;
}