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
 * 工资表
 * @TableName salary
 */
@TableName(value ="salary")
@Data
@Accessors(chain = true)
public class Salary implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 公司id
     */
    @TableField(value = "c_id")
    private Long cId;

    /**
     * 导入日期
     */
    @TableField(value = "Import_date")
    private String importDate;

    /**
     * 关联的工资类型
     */
    @TableField(value = "type_id")
    private Long typeId;

    /**
     * 员工数
     */
    @TableField(value = "staff_count")
    private Integer staffCount;

    /**
     * 总工资金额
     */
    @TableField(value = "net_amount")
    private Long netAmount;

    /**
     * 计提凭证
     */
    @TableField(value = "j_certificate_id")
    private Long jCertificateId;

    /**
     * 发放凭证
     */
    @TableField(value = "f_certificate_id")
    private Long fCertificateId;

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