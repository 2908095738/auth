package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 工资凭证规则模板表
 * @TableName voucher_rule
 */
@TableName(value ="voucher_rule")
@Data
public class VoucherRule implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 凭证类型
     */
    @TableField(value = "voucher_type")
    private Integer voucherType;

    /**
     * 凭证字
     */
    @TableField(value = "voucher_word")
    private String voucherWord;

    /**
     * 薪资类型
     */
    @TableField(value = "calculation_formula")
    private Integer calculationFormula;

    /**
     * 规则是否启用
     */
    @TableField(value = "is_enabled")
    private Integer isEnabled;

    /**
     * 规则创建的时间
     */
    @TableField(value = "created_at")
    private Date createdAt;

    /**
     * 规则最后更新的时间
     */
    @TableField(value = "updated_at")
    private Date updatedAt;

    /**
     * 0表示未删除，1表示已删除
     */
    @TableField(value = "is_deleted")
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}