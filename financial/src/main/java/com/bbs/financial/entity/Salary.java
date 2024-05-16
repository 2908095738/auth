package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 工资表
 * @TableName salary
 */
@TableName(value ="salary")
@Data
public class Salary implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 导入日期
     */
    @TableField(value = "Import_date")
    private Date importDate;

    /**
     * 关联的工资类型
     */
    @TableField(value = "type_id")
    private Integer typeId;

    /**
     * 员工数
     */
    @TableField(value = "staff_count")
    private Integer staffCount;

    /**
     * 总工资金额
     */
    @TableField(value = "net_amount")
    private BigDecimal netAmount;

    /**
     * 创建的时间
     */
    @TableField(value = "created_at")
    private Date createdAt;

    /**
     * 最后更新的时间
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