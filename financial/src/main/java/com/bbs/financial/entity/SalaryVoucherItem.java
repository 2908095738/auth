package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 当前公司核算凭证
 * @TableName salary_voucher_item
 */
@TableName(value ="salary_voucher_item")
@Data
@Accessors(chain = true)
public class SalaryVoucherItem implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联的公司凭证ID
     */
    @TableField(value = "accounting_set_id")
    private Long accountingSetId;

    /**
     * 辅助核算ID
     */
    @TableField(value = "accounting_item_type_id")
    private Long accountingItemTypeId;

    /**
     * 是否启用该项目
     */
    @TableField(value = "is_active")
    private Boolean isActive;

    /**
     * 凭证项目创建的时间
     */
    @TableField(value = "created_at")
    private Date createdAt;

    /**
     * 凭证项目最后更新的时间
     */
    @TableField(value = "updated_at")
    private Date updatedAt;

    @TableField(value = "type")
    private Integer type;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}