package com.bbs.financial.api.invoice.add;

import com.bbs.Result;
import com.bbs.enums.CodeEnum;
import com.bbs.exception.BusinessException;
import com.bbs.financial.api.certificate.add.AddCertificate;
import com.bbs.financial.api.certificate.no.search.SearchCertificateNo;
import com.bbs.financial.converter.InvoiceConverter;
import com.bbs.financial.entity.Invoice;
import com.bbs.financial.service.InvoiceService;
import com.bbs.financial.util.LoginUser;
import com.bbs.financial.util.SpringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 发票生成凭证
 */
@RestController
@RequestMapping
public class AddCertByInvoice {
    @Resource
    private DataSourceTransactionManager transactionManager;

    @Resource
    private TransactionDefinition transactionDefinition;

    @Resource
    private InvoiceConverter invoiceConverter;

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private InvoiceService invService;

    private class StringTIP {
        public static final String SPACE = "";
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {

        /**
         * 发票id
         */
        private Long invoiceId;


        /**
         * 凭证模板名称
         */
        private String tempName;

        /**
         * 开票日期
         */
        private Date openDate;

        private List<DetailParam> details;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailParam {
        /**
         * 摘要
         */
        private String certificateAbstract;

        /**
         * 科目
         */
        private Long accountId;

        /**
         * 借方金额
         */
        private String borrowMoney;

        /**
         * 贷方金额
         */
        private String loansMoney;

        /**
         * 辅助核算
         */
        private String auxiliary;
    }

    /**
     * 发票生成凭证
     */
    @PutMapping("/invoice/cert")
    public Result<String> addCert(@RequestBody Param param) {
        Invoice invoice = invoiceConverter.toEntity(param);
        AddCertificate.Param certParam = getCertParam(invoice.getOpenDate(), param.getDetails());

        Object addRespData = null;
        try {
            addRespData = SpringUtil.getRespData(AddCertificate.class, applicationContext, a -> a.add(certParam));
        } catch (RuntimeException e) {
            if (e.getCause() instanceof BusinessException)
                return Result.failed(CodeEnum.FAILED_BUSINESS, ((BusinessException) e.getCause()).getMsg());
            throw e;
        }

        if (Objects.isNull(addRespData) || !(addRespData instanceof Long))
            return Result.success(Boolean.FALSE.toString());

        Long certId = (Long) addRespData;

        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            invService.lambdaUpdate()
                    .set(Invoice::getCertificateId, certId)
                    .set(Invoice::getTempName, invoice.getTempName())
                    .eq(Invoice::getId, param.getInvoiceId())
                    .update();

            transactionManager.commit(transaction);
            return Result.success(Boolean.TRUE.toString());
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            return Result.failed(Boolean.FALSE.toString());
        }
    }

    /**
     * 获取生成凭证所需参数
     *
     * @param openDate   开票日期
     * @param detailList 发票明细摘要列表
     */
    private AddCertificate.Param getCertParam(Date openDate, List<DetailParam> detailList) {
        AddCertificate.Param param = new AddCertificate.Param();

        param.setAccountingSetId(LoginUser.getLoginSetId());
        param.setCertificateWord(NumberUtils.INTEGER_ZERO + StringTIP.SPACE);
        param.setNo(getNo(openDate));
        param.setDate(openDate);
        param.setIsNeedCertId(true);
        param.setAbstracts(getAbst(detailList));

        return param;
    }

    /**
     * 获取编号
     *
     * @param date 日期
     */
    private Long getNo(Date date) {
        SearchCertificateNo.Param reqParam = new SearchCertificateNo.Param();
        reqParam.setDate(date.getTime());

        return SpringUtil.getRespData(SearchCertificateNo.class, applicationContext, c -> c.search(reqParam));
    }

    /**
     * 获取凭证摘要列表
     *
     * @param detailList 发票明细摘要列表
     */
    private List<AddCertificate.Abstract> getAbst(List<DetailParam> detailList) {
        List<AddCertificate.Abstract> result = new ArrayList<>();

        for (DetailParam detail : detailList) {
            AddCertificate.Abstract abst = new AddCertificate.Abstract();

            abst.setCertificateAbstract(detail.getCertificateAbstract());
            abst.setAccountId(detail.getAccountId());

            if (StringUtils.isNotBlank(detail.getBorrowMoney())) {
                BigDecimal oriMoney = new BigDecimal(detail.getBorrowMoney());
                BigDecimal doneMoney = oriMoney.setScale(NumberUtils.INTEGER_ZERO, BigDecimal.ROUND_DOWN);
                abst.setBorrowMoney(doneMoney.toString());
            }
            if (StringUtils.isNotBlank(detail.getLoansMoney())) {
                BigDecimal oriMoney = new BigDecimal(detail.getLoansMoney());
                BigDecimal doneMoney = oriMoney.setScale(NumberUtils.INTEGER_ZERO, BigDecimal.ROUND_DOWN);
                abst.setLoansMoney(doneMoney.toString());
            }

            abst.setAuxiliary(detail.getAuxiliary());

            result.add(abst);
        }

        return result;
    }
}