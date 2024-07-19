package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;

/**
 * 辅助核算项目
 * @TableName account_auxiliary
 */
@TableName(value ="account_auxiliary")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class AccountAuxiliary implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 核算类型id
     */
    @TableField(value = "type_id")
    private Long typeId;

    /**
     * 账套ID
     */
    @TableField(value = "accounting_set_id")
    private Long accountingSetId;

    /**
     * 编码
     */
    @TableField(value = "no")
    private String no;

    /**
     * 名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 规格
     */
    @TableField(value = "spce")
    private String spce;

    /**
     * 单位
     */
    @TableField(value = "unit")
    private String unit;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 0表示未删除，1表示已删除
     */
    @TableField(value = "is_deleted")
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}