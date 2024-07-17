package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 记账凭证：模板
 * @TableName certificate_template
 */
@TableName(value ="certificate_template")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateTemplate implements Serializable {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 类型
     */
    @TableField(value = "type")
    private String type;

    /**
     * 名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 简介
     */
    @TableField(value = "comment")
    private String comment;

    /**
     * 公司ID
     */
    @TableField(value = "accounting_set_id")
    private Long accountingSetId;

    /**
     * 是否启用该项目：1启用，0关闭
     */
    @TableField(value = "is_active")
    private Boolean isActive;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableField(exist = false)
    private List<CertificateTemplateAbstract> templateAbstractList;
}