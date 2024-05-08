package com.bbs.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@Accessors(chain = true)
@TableName(value = "order_send")
public class OrderSend implements Serializable {

    @ApiModelProperty(value = "唯一标识符")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "物流单号")
    @TableField(value = "send_sn")
    private String sendSN;

    @ApiModelProperty(value = "物流公司(配送方式)")
    @TableField(value = "send_company")
    private String sendCompany;

    @ApiModelProperty(value = "订单id")
    @TableField(value = "order_id")
    private Long orderId;

    @ApiModelProperty(value = "订单状态：0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单")
    @TableField(value = "status")
    private Integer status;

    @ApiModelProperty(value = "订单创建时间")
    @TableField(value = "create_time")
    private Date createTime;

    private static final long serialVersionUID = 1L;
}