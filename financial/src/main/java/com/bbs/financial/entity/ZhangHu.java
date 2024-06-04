package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 账户
 *
 * @TableName zhang_hu
 */
@TableName(value = "zhang_hu")
@Data
public class ZhangHu implements Serializable {
    /**
     * 唯一标识符
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 编码
     */
    @TableField(value = "code")
    private String code;

    /**
     * 名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 币别ID
     */
    @TableField(value = "m_type_id")
    @JsonProperty(value = "mTypeId")
    private Long mTypeId;

    /**
     * 账户账号
     */
    @TableField(value = "zhang_hu_code")
    private String zhangHuCode;

    /**
     * 账户银行/机构：
     */
    @TableField(value = "bank_name")
    private String bankName;

    /**
     * 科目id
     */
    @TableField(value = "subjects_id")
    private Long subjectsId;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 账户类型：0.库存现金;1.银行存款;2.其他货币资金
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 是否启用该账户：1.启用;0.关闭
     */
    @TableField(value = "is_active")
    private Integer isActive;

    /**
     * 银企互联状态：1.启用;0.关闭
     */
    @TableField(value = "is_link")
    private Integer isLink;

    /**
     * 公司id
     */
    @TableField(value = "company_id")
    private Long companyId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}