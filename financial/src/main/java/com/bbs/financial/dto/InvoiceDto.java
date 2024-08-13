package com.bbs.financial.dto;

import com.bbs.financial.entity.InvoiceDetail;
import com.bbs.financial.enums.InvStatusEnum;
import com.bbs.financial.enums.InvTypeEnum;
import com.bbs.financial.enums.TaxTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 发票数据
 */
@Data
@ApiModel("发票数据")
public class InvoiceDto {
    /**
     * 主键
     */
    private Long id;

    /**
     * 开票日期
     */
    private Date openDate;

    /**
     * 发票代码
     */
    private String invoiceCode;

    /**
     * 发票号码
     */
    private String invoiceNumber;

    /**
     * 发票状态：{@link InvStatusEnum}
     */
    private Integer invoiceStatus;

    /**
     * 客户名称
     */
    private String clientName;

    /**
     * 金额
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal nonTaxMoney;

    /**
     * 税额
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal taxMoney;

    /**
     * 统一社会信用代码
     */
    private String creditCode;

    /**
     * 地址及电话
     */
    private String addressPhone;

    /**
     * 开户行及账户
     */
    private String openAccount;

    /**
     * 校验码后六位
     */
    private String verifyCode;

    /**
     * 备注
     */
    private String remark;

    /**
     * 凭证模板名称
     */
    private String tempName;

    /**
     * 录入发票明细：1.录入;0.不录入;
     */
    private Integer isInvoiceDetail;

    /**
     * 发票类型：{@link InvTypeEnum}
     */
    private Integer invoiceType;

    /**
     * 计税方式：{@link TaxTypeEnum}
     */
    private Integer taxType;

    /**
     * 凭证ID
     */
    private Long certificateId;

    /**
     * 认证状态：0.未认证;1.已认证;
     */
    private Boolean isAuth;

    /**
     * 制单人
     */
    private String makeName;

    /**
     * 凭证字号
     */
    private String certName;

    /**
     * 记账期间
     */
    private String noteDateStr;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 发票明细列表
     */
    private List<InvoiceDetail> details;
}