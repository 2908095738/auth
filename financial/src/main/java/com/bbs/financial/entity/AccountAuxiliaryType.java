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
 * 科目辅助核算类型
 * @TableName account_auxiliary_type
 */
@TableName(value ="account_auxiliary_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class AccountAuxiliaryType implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 公司ID
     */
    @TableField(value = "company_id")
    private Long companyId;

    /**
     * 添加人
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 0表示默认的，1表示企业自定义的
     */
    @TableField(value = "is_user_defined")
    private Integer isUserDefined;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}