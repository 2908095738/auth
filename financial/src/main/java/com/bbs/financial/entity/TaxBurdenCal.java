package com.bbs.financial.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.bbs.financial.enums.TaxBurdenCalTypeEnum;
import lombok.Data;

/**
 * 税负测算
 *
 * @TableName tax_burden_cal
 */
@TableName(value = "tax_burden_cal")
@Data
public class TaxBurdenCal implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 项目名称
     */
    @TableField(value = "item")
    private String item;

    /**
     * (不含税)金额
     */
    @TableField(value = "money")
    private BigDecimal money;

    /**
     * 税额
     */
    @TableField(value = "tax_money")
    private BigDecimal taxMoney;

    /**
     * 期数
     */
    @TableField(value = "date")
    private Date date;

    /**
     * 排序
     */
    @TableField(value = "sort")
    private Integer sort;

    /**
     * 税负测算类型: {@link TaxBurdenCalTypeEnum}
     */
    @TableField(value = "type")
    @JSONField(serialzeFeatures = SerializerFeature.WriteEnumUsingToString)
    private TaxBurdenCalTypeEnum type;

    /**
     * 公司ID
     */
    @TableField(value = "accounting_set_id")
    private Long accountingSetId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}