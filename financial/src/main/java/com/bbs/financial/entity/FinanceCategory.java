package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 资产类别
 * @TableName finance_category
 */
@TableName(value ="finance_category")
@Data
public class FinanceCategory implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 类别编码
     */
    @TableField(value = "f_category_code")
    private String fCategoryCode;

    /**
     * 类别名称
     */
    @TableField(value = "f_category_name")
    private String fCategoryName;

    /**
     * 折旧方法状态：
     */
    @TableField(value = "old_status")
    private Integer oldStatus;

    /**
     * 预计使用年限
     */
    @TableField(value = "future_year")
    private Integer futureYear;

    /**
     * 预计净残值率
     */
    @TableField(value = "future_price")
    private BigDecimal futurePrice;

    /**
     * 固定资产科目
     */
    @TableField(value = "lock_finance")
    private String lockFinance;

    /**
     * 累计折扣科目
     */
    @TableField(value = "total_finance")
    private String totalFinance;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}