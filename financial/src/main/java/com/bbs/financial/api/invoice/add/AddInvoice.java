package com.bbs.financial.api.invoice.add;

import com.bbs.Result;
import com.bbs.financial.converter.InvoiceConverter;
import com.bbs.financial.converter.InvoiceDetailConverter;
import com.bbs.financial.entity.Invoice;
import com.bbs.financial.entity.InvoiceDetail;
import com.bbs.financial.enums.InvCateEnum;
import com.bbs.financial.enums.InvStatusEnum;
import com.bbs.financial.enums.InvTypeEnum;
import com.bbs.financial.enums.TaxTypeEnum;
import com.bbs.financial.service.InvoiceDetailService;
import com.bbs.financial.service.InvoiceService;
import com.bbs.financial.util.LoginUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping
public class AddInvoice {
    @Resource
    private DataSourceTransactionManager transactionManager;

    @Resource
    private TransactionDefinition transactionDefinition;

    @Resource
    private InvoiceConverter invConverter;

    @Resource
    private InvoiceDetailConverter detailConverter;

    @Resource
    private InvoiceService orm;

    @Resource
    private InvoiceDetailService detailORM;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {
        /**
         * 开票日期
         */
        @NotNull
        private Date openDate;

        /**
         * 发票代码
         */
        @NotBlank
        private String invoiceCode;

        /**
         * 发票号码
         */
        @NotBlank
        private String invoiceNumber;

        /**
         * 发票状态：{@link InvStatusEnum}
         */
        @NotNull
        private Integer invoiceStatus;

        /**
         * 客户名称
         */
        @NotNull
        private String clientName;

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
         * 录入发票明细：0.录入;1.不录入;
         */
        @NotNull
        private Boolean isInvoiceDetail;

        /**
         * 发票类型：{@link InvTypeEnum}
         */
        @NotNull
        private Integer invoiceType;

        /**
         * 发票分类：{@link InvCateEnum}
         */
        @NotNull
        private Integer invoiceCategory;

        /**
         * 计税方式：{@link TaxTypeEnum}
         */
        private Integer taxType;

        /**
         * 发票明细
         */
        private List<DetailParam> details;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailParam {
        /**
         * 商品名称
         */
        private String name;

        /**
         * 规格型号/车牌号/车辆识别代号
         */
        private String code;

        /**
         * 单位
         */
        private String unit;

        /**
         * 数量
         */
        private Integer quantity;

        /**
         * 单价(也作为新增-费用小票-发票类型-飞机票-发票数据-机票Plus燃油费)
         */
        private String price;

        /**
         * 税率
         */
        private String taxRates;

        /**
         * 不含税金额
         */
        @NotBlank
        private String nonTaxMoney;

        /**
         * 税额
         */
        private String taxMoney;

        /**
         * 发票明细辅助核算id
         */
        private Long abstAuxId;
    }

    /**
     * 新增发票
     */
    @PutMapping("/invoice")
    public Result<Boolean> add(@RequestBody Param param) {
        Invoice invoice = invConverter.toEntity(param);
        invoice.setIsInvoiceDetail(param.getIsInvoiceDetail() ? NumberUtils.INTEGER_ONE : NumberUtils.INTEGER_ZERO);
        invoice.setCreateBy(LoginUser.getId());
        invoice.setAccountingSetId(LoginUser.getLoginSetId());

        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            orm.save(invoice);//保存发票
            saveDatails(param.getDetails(), invoice.getId());
            transactionManager.commit(transaction);
            return Result.success();
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new RuntimeException(e);
        }
    }

    /**
     * 保存发票明细
     *
     * @param detailList 发票明细列表
     * @param invoiceId  发票id
     */
    public void saveDatails(List<DetailParam> detailList, Long invoiceId) {
        detailORM.saveBatch(detailList.stream().map(p -> {
            InvoiceDetail entity = detailConverter.toEntity(p);
            entity.setInvoiceId(invoiceId);

            if (StringUtils.isNotBlank(p.getPrice()))
                entity.setPrice(new BigDecimal(p.getPrice()));
            if (StringUtils.isNotBlank(p.getTaxRates()))
                entity.setTaxRates(new BigDecimal(p.getTaxRates()));

            return entity;
        }).collect(Collectors.toList()));
    }
}