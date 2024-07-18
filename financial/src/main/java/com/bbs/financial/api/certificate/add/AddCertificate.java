package com.bbs.financial.api.certificate.add;

import cn.hutool.core.date.DateUtil;
import com.bbs.Result;
import com.bbs.enums.financial.CertificateWordEnum;
import com.bbs.financial.converter.CertificateConverter;
import com.bbs.financial.entity.*;
import com.bbs.financial.service.*;
import com.bbs.financial.service.impl.LedgerGeneralServiceImpl;
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
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.*;

@RestController
@RequestMapping
public class AddCertificate {

    @Resource
    private CertificateConverter converter;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {

        private Long accountingSetId;
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

        boolean useOldData;
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
    @Resource
    private CertificateFileService certificateFileService;
    @Resource
    private LedgerGeneralService ledgerGeneralService;
    @Resource
    private AccountService accountService;
    @Resource
    private LedgerSubsidiaryService ledgerSubsidiaryService;

    @PutMapping("/certificate")
    public Result<Boolean> add(@RequestBody Param param) {
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        Long accountingSetId = LoginUser.getLoginSetId();
        param.setAccountingSetId(accountingSetId);
        Certificate certificate = converter.toEntity(param);
        certificate.setCertificateWord(CertificateWordEnum.RECORD);
        certificate.setCreateBy(LoginUser.getId());
        try {
            // 保存凭证
            saveCertificate(certificate);
            // 保存具体科目信息
            List<CertificateAbstract> certificateAbstracts = saveAbstracts(param, certificate);
            // 并更新总账（修改账户金额）、日记账
            updateGeneralLedgerAndLedgerSubsidiary(certificate, certificateAbstracts, param.useOldData);
            // 修改指定【凭证附件】的凭证 ID（原因：添加附件时，未创建凭证，只能先绑定到日期、凭证字、编号）
            updateFiles(param, certificate);
            transactionManager.commit(transaction);
            return Result.success();
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new RuntimeException(e);
        }
    }

    private void saveCertificate(Certificate certificate) {
        db.save(certificate);
    }

    private List<CertificateAbstract> saveAbstracts(Param param, Certificate certificate) {
        List<CertificateAbstract> certificateAbstracts = param.abstracts.stream().map(certificateAbstract -> {
            CertificateAbstract entity = new CertificateAbstract();
            entity.setCertificateId(certificate.getId());
            entity.setAccountId(certificateAbstract.getAccountId());
            if (nonNull(certificateAbstract.getBorrowMoney()))
                entity.setBorrowMoney(Long.valueOf(certificateAbstract.getBorrowMoney().replace(",", "")));
            if (nonNull(certificateAbstract.getLoansMoney()))
                entity.setLoansMoney(Long.valueOf(certificateAbstract.getLoansMoney().replace(",", "")));
            entity.setCertificateAbstract(certificateAbstract.getCertificateAbstract());
            return entity;
        }).collect(Collectors.toList());
        certificateAbstractService.saveBatch(certificateAbstracts);
        return certificateAbstracts;
    }

    private void updateGeneralLedgerAndLedgerSubsidiary(Certificate certificate, List<CertificateAbstract> certificateAbstracts, Boolean useOldData) {
        Date now = new Date();
        // 准备工作 1：Map<需要更新的科目ID, 会计科目> 用于后续总账，补充科目信息
        Map<Long, Account> accountIdMap = Account.converterToIdMap(searchNeedUpdateAccount(certificateAbstracts));

        // 1.1 计算需要增加的借方和贷方金额
        long borrowMoney = LONG_ZERO;
        long loansMoney = LONG_ZERO;
        List<LedgerSubsidiary> needUpdateLedgerSubsidiary = new ArrayList<>();
        List<LedgerGeneral> needUpdateLedgerGeneral = new ArrayList<>();
        for (CertificateAbstract certificateAbstract : certificateAbstracts) {
            Account account = accountIdMap.get(certificateAbstract.getAccountId());
            loansMoney += certificateAbstract.getLoansMoney();
            borrowMoney += certificateAbstract.getBorrowMoney();
            needUpdateLedgerSubsidiary.add(
                    LedgerSubsidiary.createLedgerSubsidiary(account, certificate, certificateAbstract, borrowMoney, loansMoney)
            );
            // 先尝试初始化总账（包括年初余额、期初余额、本期合计、本年累计），再计算总账
            LedgerGeneralServiceImpl.AccountLedgerGeneral accountLedgerGeneral = ledgerGeneralService.tryInitAccountLedgerGeneral(now, account, useOldData);// throw DataMissingException
            needUpdateLedgerGeneral.addAll(
                    ledgerGeneralService.computeAccountCurrentTotal(now, account, accountLedgerGeneral.getCurrentTotal(), borrowMoney, loansMoney)
            );
        }
        ledgerSubsidiaryService.saveBatch(needUpdateLedgerSubsidiary);
        ledgerGeneralService.updateBatchById(needUpdateLedgerGeneral);
    }

    private List<Account> searchNeedUpdateAccount(List<CertificateAbstract> certificateAbstracts) {
        return accountService.listByIds(certificateAbstracts.stream().map(CertificateAbstract::getAccountId).collect(Collectors.toList()));
    }


    private void updateFiles(Param param, Certificate certificate) {
        certificateFileService.lambdaUpdate()
                .set(CertificateFile::getCertificateId, certificate.getId())
                .eq(CertificateFile::getAccountingSetId, param.getAccountingSetId())
                .eq(CertificateFile::getCertificateWord, param.certificateWord)
                .eq(CertificateFile::getNo, param.no)
                .ge(CertificateFile::getDate, DateUtil.beginOfMonth(param.date))
                .lt(CertificateFile::getDate, DateUtil.beginOfMonth(DateUtil.offsetMonth(param.date, INTEGER_ONE)))
                .update();
    }
}
