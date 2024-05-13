package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 科目备注（用于记账凭证 -> 摘要）
 * @TableName account_remark
 */
@TableName(value ="account_remark")
@Data
public class AccountRemark implements Serializable {
    /**
     * 编号
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 公司ID
     */
    @TableField(value = "company_id")
    private Long companyId;

    /**
     * 科目ID
     */
    @TableField(value = "account_id")
    private Long accountId;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}