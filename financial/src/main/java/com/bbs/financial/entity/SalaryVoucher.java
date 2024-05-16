package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 工资凭证表
 * @TableName salary_voucher
 */
@TableName(value ="salary_voucher")
@Data
public class SalaryVoucher implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 凭证编号:日期+凭证字+每月第几个
     */
    @TableField(value = "voucher_no")
    private String voucherNo;

    /**
     * 关联的工资导入表ID
     */
    @TableField(value = "salary_id")
    private Integer salaryId;

    /**
     * 工资凭证的日期
     */
    @TableField(value = "voucher_date")
    private Date voucherDate;

    /**
     * 工资凭证的摘要说明
     */
    @TableField(value = "voucher_summary")
    private String voucherSummary;

    /**
     * 附件文件的ID，如PDF或图片
     */
    @TableField(value = "attachment_id")
    private Integer attachmentId;

    /**
     * 工资凭证创建的时间
     */
    @TableField(value = "created_at")
    private Date createdAt;

    /**
     * 工资凭证最后更新的时间
     */
    @TableField(value = "updated_at")
    private Date updatedAt;

    /**
     * 凭证类型:1计提工资凭证，2实发工资凭证
     */
    @TableField(value = "voucher_type")
    private Integer voucherType;

    /**
     * 创建人
     */
    @TableField(value = "create_name")
    private String createName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}