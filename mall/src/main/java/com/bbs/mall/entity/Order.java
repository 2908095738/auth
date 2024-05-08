package com.bbs.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单实体
 */
@Data
@Accessors(chain = true)
@TableName(value = "order")
public class Order implements Serializable {
    @ApiModelProperty(value = "唯一标识符")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "订单编号")
    @TableField(value = "order_sn")
    private String orderSN;

    @ApiModelProperty(value = "订单总金额")
    @TableField(value = "total_amount")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "运费金额")
    @TableField(value = "send_amount")
    private BigDecimal sendAmount;

    @ApiModelProperty(value = "促销后金额")
    @TableField(value = "low_amount")
    private BigDecimal lowAmount;

    @ApiModelProperty(value = "应付金额(实际支付金额)")
    @TableField(value = "pay_amount")
    private BigDecimal payAmount;

    @ApiModelProperty(value = "促销信息json")
    @TableField(value = "low_msg_json")
    private String lowMsgJson;

    @ApiModelProperty(value = "支付方式：0->未支付；1->支付宝；2->微信")
    @TableField(value = "payType")
    private Integer payType;

    @ApiModelProperty(value = "订单状态：0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单")
    @TableField(value = "status")
    private Integer status;

    @ApiModelProperty(value = "发票类型：0->不开发票；1->电子发票；2->纸质发票")
    @TableField(value = "bill_type")
    private Integer billType;

    @ApiModelProperty(value = "发票抬头")
    @TableField(value = "bill_header")
    private String billHeader;

    @ApiModelProperty(value = "发票内容")
    @TableField(value = "bill_content")
    private String billContent;

    @ApiModelProperty(value = "收票人电话")
    @TableField(value = "bill_get_phone")
    private String billGetPhone;

    @ApiModelProperty(value = "收票人邮箱")
    @TableField(value = "bill_get_email")
    private String billGetEmail;

    @ApiModelProperty(value = "收货人姓名")
    @TableField(value = "shou_name")
    private String shouName;

    @ApiModelProperty(value = "收货人电话")
    @TableField(value = "shou_phone")
    private String shouPhone;

    @ApiModelProperty(value = "收货人邮编")
    @TableField(value = "shou_post_code")
    private String shouPostCode;

    @ApiModelProperty(value = "省份/直辖市")
    @TableField(value = "shou_province")
    private String shouProvince;

    @ApiModelProperty(value = "城市")
    @TableField(value = "shou_city")
    private String shouCity;

    @ApiModelProperty(value = "区")
    @TableField(value = "shou_region")
    private String shouRegion;

    @ApiModelProperty(value = "详细地址")
    @TableField(value = "shou_detail")
    private String shouDetail;

    @ApiModelProperty(value = "自动确认天数")
    @TableField(value = "auto_shou_day")
    private Integer autoShouDay;

    @ApiModelProperty(value = "确认收货状态：0->未确认；1->已确认")
    @TableField(value = "shou_status")
    private Integer shouStatus;

    @ApiModelProperty(value = "删除状态：0->未删除；1->逻辑删除")
    @TableField(value = "del_status")
    private Integer delStatus;

    @ApiModelProperty(value = "用户id")
    @TableField(value = "user_id")
    private Long userId;

    @ApiModelProperty(value = "品牌id")
    @TableField(value = "brand_id")
    private Long brandId;

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "create_time")
    private Date createTime;

    @ApiModelProperty(value = "支付时间")
    @TableField(value = "pay_time")
    private Date payTime;

    @ApiModelProperty(value = "发货时间")
    @TableField(value = "send_time")
    private Date sendTime;

    @ApiModelProperty(value = "确认收货时间")
    @TableField(value = "shou_time")
    private Date shouTime;

    @ApiModelProperty(value = "修改时间")
    @TableField(value = "update_time")
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}