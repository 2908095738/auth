package com.bbs.financial.entity;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.List;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 科目
 * @TableName account
 */
@TableName(value ="account_2")
@Data
@Accessors(chain = true)
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
     * 辅助核算段
     */
    @TableField(value = "auxiliary_calculation")
    private String auxiliaryCalculation;

    /**
     * 公司ID
     */
    @TableField(value = "company_id")
    private Long companyId;

    /**
     * 上级ID
     */
    @TableField(value = "parent_id")
    private Long parentId;

    /**
     * 权重
     */
    @TableField(value = "weight")
    private Integer weight;

    /**
     * 级别
     */
    @TableField(value = "level")
    private Integer level;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableField(exist = false)
    private AccountRemark remark;

    /**
     * 父级名称
     */
    @TableField(exist = false)
    private String parentName;

    /**
     * 子级
     */
    @TableField(exist = false)
    private List<Tree<String>> children;

    /**
     * label
     */
    @TableField(exist = false)
    private String label;
}