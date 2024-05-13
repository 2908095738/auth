package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 科目
 * @TableName account
 */
@TableName(value ="account")
@Data
@EqualsAndHashCode(callSuper = true)
public class Account extends Model<Account> implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 科目类别
     */
    @TableField(value = "account_sort")
    private String accountSort;

    /**
     * 编号
     */
    @TableField(value = "no")
    private String no;

    /**
     * 会计科目名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 类别
     */
    @TableField(value = "sort")
    private String sort;

    /**
     * 方向
     */
    @TableField(value = "direction")
    private String direction;

    /**
     * 外币核算
     */
    @TableField(value = "currency")
    private String currency;

    /**
     * 期末调汇
     */
    @TableField(value = "period_exchange_rate_adjust")
    private String periodExchangeRateAdjust;

    /**
     * 二级科目
     */
    @TableField(value = "second_level_account")
    private String secondLevelAccount;

    /**
     * 三级科目
     */
    @TableField(value = "three_level_account")
    private String threeLevelAccount;

    /**
     * 四级科目
     */
    @TableField(value = "four_level_account")
    private String fourLevelAccount;

    /**
     * 五级科目
     */
    @TableField(value = "five_level_account")
    private String fiveLevelAccount;

    /**
     * 辅助核算段
     */
    @TableField(value = "auxiliary_calculation")
    private String auxiliaryCalculation;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableField(exist = false)
    private AccountRemark remark;
}