package com.bbs.financial.dto;

import com.bbs.financial.entity.InvoiceDetail;
import com.bbs.financial.enums.InvStatusEnum;
import com.bbs.financial.enums.InvTypeEnum;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@ApiModel("发票表格数据")
@NoArgsConstructor
@AllArgsConstructor
public class ExcelInvoiceDto {
    /**
     * 开票日期
     */
    private String openDateStr;

    /**
     * 发票类型：{@link InvTypeEnum}
     */
    private String invoiceTypeStr;

    /**
     * 发票代码
     */
    private String invoiceCode;

    /**
     * 发票号码
     */
    private String invoiceNumber;

    /**
     * 客户名称
     */
    private String clientName;

    /**
     * 不含税金额字符串
     */
    private String nonTaxMoneyStr;

    /**
     * 税额字符串
     */
    private String taxMoneyStr;

    /**
     * 价税合计字符串
     */
    private String taxTotalStr;

    /**
     * 发票影像
     */
    private String invoiceImage;

    /**
     * 凭证模板
     */
    private String tempName;

    /**
     * 发票状态：{@link InvStatusEnum}
     */
    private String invoiceStatusStr;

    /**
     * 制单人
     */
    private String makeName;

    /**
     * 创建时间
     */
    private String createTimeStr;

    /**
     * 凭证字号
     */
    private String certName;

    /**
     * 记账期间
     */
    private String noteDateStr;

    /**
     * 校验码后六位
     */
    private String verifyCode;

    /**
     * 查验结果
     */
    private String result;

    /**
     * 本批发票说明
     */
    private String theInvoiceRead;

    /**
     * 发票明细列表
     */
    private List<InvoiceDetail> details;
}