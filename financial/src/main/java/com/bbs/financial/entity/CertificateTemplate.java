package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.yulichang.annotation.EntityMapping;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

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
@FieldNameConstants
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
     * 凭证字
     */
    @TableField(value = "certificate_word")
    private String certificateWord;

    /**
     * 是否默认模板：1.默认;0.非默认
     */
    @TableField(value = "is_default")
    private Boolean isDefault;

    /**
     * 发票分类：0.销项发票;1.进项发票;2.费用小票;
     */
    @TableField(value = "invoice_category")
    private Integer invoiceCategory;

    /**
     * 是否启用该项目：1启用，0关闭
     */
    @TableField(value = "is_active")
    private Boolean isActive;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableField(exist = false)
    @EntityMapping(
            thisField = Fields.id,
            joinField = CertificateTemplateAbstract.Fields.templateId
    )
    private List<CertificateTemplateAbstract> templateAbstractList;
}