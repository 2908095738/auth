package com.bbs.financial.api.certificate.template;

import com.bbs.Result;
import com.bbs.financial.converter.CertificateTemplateConverter;
import com.bbs.financial.entity.CertificateTemplate;
import com.bbs.financial.entity.CertificateTemplateAbstract;
import com.bbs.financial.service.CertificateTemplateAbstractService;
import com.bbs.financial.service.CertificateTemplateService;
import com.bbs.financial.util.LoginUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequestMapping
@RestController
public class AddTemplate {

    @Resource
    private CertificateTemplateService db;
    @Resource
    private CertificateTemplateConverter converter;
    @Resource
    private CertificateTemplateAbstractService certificateTemplateAbstractService;
    @Resource
    private TransactionDefinition transactionDefinition;
    @Resource
    private DataSourceTransactionManager transactionManager;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Param {

        /**
         * 类型
         */
        private String type;

        /**
         * 名称
         */
        private String name;

        /**
         * 简介
         */
        private String comment;

        /**
         * 凭证字
         */
        private String certificateWord;

        /**
         * 发票分类：0.销项发票;1.进项发票;2.费用小票;
         */
        private Integer invoiceCategory;

        /**
         * 具体科目摘要
         */
        private List<TemplateAbstract> abstractList;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TemplateAbstract {
        /**
         * 摘要
         */
        private String certificateAbstract;

        /**
         * 科目ID
         */
        private Long accountId;

        /**
         * 辅助核算
         */
        private String auxiliary;

        /**
         * 数量
         */
        private Long number;

        /**
         * 币别ID
         */
        private Long moneyTypeId;

        /**
         * 借方金额
         */
        private String borrowMoney;

        /**
         * 贷方金额
         */
        private String loansMoney;

        /**
         * 借贷类型：1借0贷，默认借
         */
        private Integer borrowOrLoansType;

        /**
         * 取值类型：0.价税合计;1.税额;2.不含税金额;
         */
        private Integer moneyType;

        /**
         * 权重
         */
        private Integer weight;
    }

    @PutMapping("/certificate/template")
    public Result<Boolean> add(@RequestBody Param param) {
        Long loginSetId = LoginUser.getLoginSetId();
        CertificateTemplate entity = converter.toEntity(param);
        entity.setAccountingSetId(loginSetId);
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            db.save(entity);
            certificateTemplateAbstractService.saveBatch(param.abstractList.stream().map(templateAbstract -> {
                CertificateTemplateAbstract certificateTemplateAbstractEntity = converter.toEntity(templateAbstract);
                certificateTemplateAbstractEntity.setTemplateId(entity.getId());
                certificateTemplateAbstractEntity.setBorrowMoney(Long.valueOf(templateAbstract.getBorrowMoney().replace(",", "")));
                certificateTemplateAbstractEntity.setLoansMoney(Long.valueOf(templateAbstract.getLoansMoney().replace(",", "")));
                return certificateTemplateAbstractEntity;
            }).collect(Collectors.toList()));
            transactionManager.commit(transaction);
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new RuntimeException(e);
        }
        return Result.success();
    }
}
