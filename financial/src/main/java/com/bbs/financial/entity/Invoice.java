package com.bbs.financial.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.bbs.api.auth.User;
import com.bbs.financial.enums.InvCateEnum;
import com.bbs.financial.enums.InvStatusEnum;
import com.bbs.financial.enums.InvTypeEnum;
import com.bbs.financial.enums.TaxTypeEnum;
import com.github.yulichang.annotation.EntityMapping;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

/**
 * 发票
 *
 * @TableName invoice
 */
@Data
@TableName(value = "invoice")
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
public class Invoice implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 开票日期
     */
    @TableField(value = "open_date")
    private Date openDate;

    /**
     * 发票代码
     */
    @TableField(value = "invoice_code")
    private String invoiceCode;

    /**
     * 发票号码
     */
    @TableField(value = "invoice_number")
    private String invoiceNumber;

    /**
     * 发票状态：{@link InvStatusEnum}
     */
    @TableField(value = "invoice_status")
    @JSONField(serialzeFeatures = SerializerFeature.WriteEnumUsingToString)
    private InvStatusEnum invoiceStatus;

    /**
     * 客户名称
     */
    @TableField(value = "client_name")
    private String clientName;

    /**
     * 统一社会信用代码
     */
    @TableField(value = "credit_code")
    private String creditCode;

    /**
     * 地址及电话
     */
    @TableField(value = "address_phone")
    private String addressPhone;

    /**
     * 开户行及账户
     */
    @TableField(value = "open_account")
    private String openAccount;

    /**
     * 校验码后六位
     */
    @TableField(value = "verify_code")
    private String verifyCode;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;


    /**
     * 凭证模板名称
     */
    @TableField(value = "temp_name")
    private String tempName;

    /**
     * 录入发票明细：1.录入;0.不录入;
     */
    @TableField(value = "is_invoice_detail")
    private Integer isInvoiceDetail;

    /**
     * 发票类型：{@link InvTypeEnum}
     */
    @TableField(value = "invoice_type")
    @JSONField(serialzeFeatures = SerializerFeature.WriteEnumUsingToString)
    private InvTypeEnum invoiceType;

    /**
     * 发票分类：{@link InvCateEnum}
     */
    @TableField(value = "invoice_category")
    @JSONField(serialzeFeatures = SerializerFeature.WriteEnumUsingToString)
    private InvCateEnum invoiceCategory;

    /**
     * 计税方式：{@link TaxTypeEnum}
     */
    @TableField(value = "tax_type")
    @JSONField(serialzeFeatures = SerializerFeature.WriteEnumUsingToString)
    private TaxTypeEnum taxType;

    /**
     * 凭证ID
     */
    @TableField(value = "certificate_id")
    private Long certificateId;

    /**
     * 认证状态：0.未认证;1.已认证;
     */
    @TableField(value = "is_auth")
    private Boolean isAuth;

    /**
     * 认证日期
     */
    @TableField(value = "auth_date")
    private Date authDate;

    /**
     * 信息审核人
     */
    @TableField(value = "auth_by")
    private Long authBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 信息创建人
     */
    @TableField(value = "create_by")
    private Long createBy;

    /**
     * 修改时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 信息修改人
     */
    @TableField(value = "update_by")
    private Long updateBy;

    /**
     * 公司ID
     */
    @TableField(value = "accounting_set_id")
    private Long accountingSetId;

    @TableField(exist = false)
    private User createUser;

    @TableField(exist = false)
    private User updateUser;

    @TableField(exist = false)
    @EntityMapping(
            thisField = Invoice.Fields.id,
            joinField = InvoiceDetail.Fields.invoiceId
    )
    private List<InvoiceDetail> details;

    @TableField(exist = false)
    @EntityMapping(thisField = Invoice.Fields.certificateId, joinField = Certificate.Fields.id)
    private Certificate certificate;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}