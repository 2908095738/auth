package com.bbs.financial.api.certificate.add;

import cn.hutool.core.date.DateUtil;
import com.bbs.Result;
import com.bbs.enums.financial.CertificateWordEnum;
import com.bbs.financial.converter.CertificateConverter;
import com.bbs.financial.entity.*;
import com.bbs.financial.enums.AccountAbstractEnum;
import com.bbs.financial.service.*;
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
    @Resource
    private CertificateFileService certificateFileService;
    @Resource
    private LedgerGeneralService ledgerGeneralService;
    @Resource
    private AccountService accountService;

    @PutMapping("/certificate")
    public Result<Boolean> add(@RequestBody Param param) {
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        Certificate certificate = converter.toEntity(param);
        certificate.setCertificateWord(CertificateWordEnum.RECORD);
        certificate.setCreateBy(LoginUser.getId());
        certificate.setCompanyId(LoginUser.getCompanyId());
        try {
            // 保存凭证
            saveCertificate(certificate);
            // 保存具体科目信息
            List<CertificateAbstract> certificateAbstracts = saveAbstracts(param, certificate);
            // 并更新总账（修改账户金额）、日记账
            updateGeneralLedgerAndLedgerSubsidiary(certificate, certificateAbstracts);
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

    private void updateGeneralLedgerAndLedgerSubsidiary(Certificate certificate, List<CertificateAbstract> certificateAbstracts) {
        Date now = new Date();
        // 准备工作 1：将凭证子项列表由 List 分组为 Map<需要更新的科目ID, List<凭证具体行>>
        Map<Long, List<CertificateAbstract>> certificateAbstractAccountIdGroups = certificateAbstracts.stream().collect(Collectors.groupingBy(CertificateAbstract::getAccountId));
        // 准备工作 2：Map<需要更新的科目ID, 会计科目> 用于后续总账，补充科目信息
        Map<Long, Account> accountIdMap = Account.converterToIdMap(searchNeedUpdateAccount(certificateAbstractAccountIdGroups));
        // 1. 查找总账中对应的科目记录，并分组为 Map<需要更新的科目ID, 总账记录>
        Map<Long, LedgerGeneral> ledgerGeneralAccountIdGroups = searchNeedUpdateLedgerGeneralGroupByAccountId(now, certificateAbstractAccountIdGroups.keySet());

        // 2. 根据需要变更的科目 ID 集合，遍历处理相关总账
        for (Map.Entry<Long, List<CertificateAbstract>> accountIdGroup: certificateAbstractAccountIdGroups.entrySet()) {
            Long accountId = accountIdGroup.getKey();
            List<CertificateAbstract> abstracts = accountIdGroup.getValue();
            Account account = accountIdMap.get(accountId);

            // 2.1 计算需要增加的借方和贷方金额
            long borrowMoney = LONG_ZERO;
            long loansMoney = LONG_ZERO;
            for (CertificateAbstract certificateAbstract : abstracts) {
                loansMoney += certificateAbstract.getLoansMoney();
                borrowMoney += certificateAbstract.getBorrowMoney();
                // 2.2 记录日记账
                LedgerSubsidiary.createLedgerSubsidiary(account, certificate, certificateAbstract, borrowMoney, loansMoney);
            }
            // 2.3 更新总账：查询总账中是否存在对应科目，如果存在则更新余额，不存在就插入初始数据
            if(ledgerGeneralAccountIdGroups.containsKey(accountId)) {
                // 2.3.1 更新余额
                LedgerGeneral ledgerGeneral = ledgerGeneralAccountIdGroups.get(accountId);
                ledgerGeneral.updateAccountBalance(borrowMoney, loansMoney);
            } else {
                // 2.3.2 插入初始数据
                LedgerGeneral.initAccountLedgerGeneral(account);
            }
        }
    }

    private List<Account> searchNeedUpdateAccount(Map<Long, List<CertificateAbstract>> certificateAbstractAccountIdGroups) {
        return accountService.listByIds(certificateAbstractAccountIdGroups.keySet());
    }

    private Map<Long, LedgerGeneral> searchNeedUpdateLedgerGeneralGroupByAccountId(Date date, Collection<Long> accountIds) {
        return ledgerGeneralService.lambdaQuery()
                // 筛选会计期间
                .ge(LedgerGeneral::getCreateTime, DateUtil.beginOfMonth(date))
                .lt(LedgerGeneral::getCreateTime, DateUtil.endOfMonth(date))
                // 筛选公司
                .eq(LedgerGeneral::getCompanyId, LoginUser.getCompanyId())
                // 筛选科目
                .in(LedgerGeneral::getAccountId, accountIds)
                // 筛选【本期合计】
                .eq(LedgerGeneral::getCertificateAbstract, AccountAbstractEnum.CURRENT_TOTAL.getName())
                .list().stream().collect(Collectors.toMap(LedgerGeneral::getAccountId, ledgerGeneral -> ledgerGeneral));
    }

    private void updateFiles(Param param, Certificate certificate) {
        certificateFileService.lambdaUpdate()
                .set(CertificateFile::getCertificateId, certificate.getId())
                .eq(CertificateFile::getCompanyId, LoginUser.getCompanyId())
                .eq(CertificateFile::getCertificateWord, param.certificateWord)
                .eq(CertificateFile::getNo, param.no)
                .ge(CertificateFile::getDate, DateUtil.beginOfMonth(param.date))
                .lt(CertificateFile::getDate, DateUtil.beginOfMonth(DateUtil.offsetMonth(param.date, INTEGER_ONE)))
                .update();
    }
}
