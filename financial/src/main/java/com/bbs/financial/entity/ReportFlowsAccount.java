package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 报表：现金流量项 & 科目关联
 * @TableName report_flows_account
 */
@TableName(value ="report_flows_account")
@Data
public class ReportFlowsAccount implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 现金流量项编码
     */
    @TableField(value = "code")
    private String code;

    /**
     * 科目ID
     */
    @TableField(value = "account_id")
    private Long accountId;

    /**
     * 是否为默认设置
     */
    @TableField(value = "is_default")
    private Integer isDefault;

    /**
     * 公司ID
     */
    @TableField(value = "company_id")
    private Long companyId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public ReportFlowsAccount(String code, Long accountId, Integer isDefault, Long companyId) {
        this.code = code;
        this.accountId = accountId;
        this.isDefault = isDefault;
        this.companyId = companyId;
    }
}