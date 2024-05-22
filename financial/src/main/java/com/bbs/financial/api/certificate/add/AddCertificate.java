package com.bbs.financial.api.certificate.add;

import com.bbs.Result;
import com.bbs.enums.financial.CertificateWordEnum;
import com.bbs.financial.converter.CertificateConverter;
import com.bbs.financial.entity.Certificate;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.service.CertificateAbstractService;
import com.bbs.financial.service.CertificateService;
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
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping
public class AddCertificate {

    @Resource
    private CertificateConverter converter;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {

        /**
         * 公司ID
         */
        private Long companyId;

        /**
         * 凭证字
         */
        private String certificateWord;

        /**
         * 编号（凭证号）
         */
        private Long no;

        /**
         * 日期
         */
        private Date date;

        /**
         * 具体科目
         */
        List<Abstract> abstracts;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Abstract {
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
         * 权重
         */
        private Integer weight;
    }

    @Resource
    private CertificateService db;
    @Resource
    private CertificateAbstractService certificateAbstractService;
    @Resource
    private TransactionDefinition transactionDefinition;
    @Resource
    private DataSourceTransactionManager transactionManager;

    @PutMapping("/certificate")
    public Result<Boolean> add(@RequestBody Param param) {
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        Certificate certificate = converter.toEntity(param);
        certificate.setCertificateWord(CertificateWordEnum.RECORD);
        certificate.setCreateBy(LoginUser.getId());
        try {
            db.save(certificate);
            certificateAbstractService.saveBatch(param.abstracts.stream().map(certificateAbstract -> {
                CertificateAbstract entity = new CertificateAbstract();
                entity.setCertificateId(certificate.getId());
                entity.setAccountId(certificateAbstract.getAccountId());
                entity.setBorrowMoney(Long.valueOf(certificateAbstract.getBorrowMoney().replace(",", "")));
                entity.setLoansMoney(Long.valueOf(certificateAbstract.getLoansMoney().replace(",", "")));
                entity.setCertificateAbstract(certificateAbstract.getCertificateAbstract());
                return entity;
            }).collect(Collectors.toList()));
            transactionManager.commit(transaction);
            return Result.success();
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new RuntimeException(e);
        }
    }
}
