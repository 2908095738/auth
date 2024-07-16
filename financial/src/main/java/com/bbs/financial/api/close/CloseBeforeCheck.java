package com.bbs.financial.api.close;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bbs.Result;
import com.bbs.financial.api.accountBook.SearchAccountTree;
import com.bbs.financial.entity.*;
import com.bbs.financial.service.AccountService;
import com.bbs.financial.service.CertificateAbstractService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.*;

import static com.bbs.financial.enums.AccountIdEnum.*;
import static org.apache.commons.lang3.math.NumberUtils.LONG_ZERO;

@RequestMapping
@RestController
public class CloseBeforeCheck {

    @Resource
    private CertificateAbstractService certificateAbstractService;
    @Resource
    private AccountService accountService;
    @Resource
    private SearchAccountTree searchAccountTree;

    @GetMapping("/close/before/check")
    public Result<Boolean> financialInitDataIsBalanced() {
        Date now = new Date();
        // 1. 准备初始数据：确保所有的初始数据已经录入系统
        List<CertificateAbstract> initialData = preparingInitialData(now);

        // 2. 编制试算平衡表：列出所有会计科目的借方和贷方余额，确保总借方余额等于总贷方余额
        TrialBalance trialBalance = generateTrialBalance(initialData);

        // 3. 核对总账与明细账：确保每个总账科目和其对应的明细账之间的数据是一致的
        checkGeneralLedgerAndSubsidiaryLedger();
        // 4. 检查资产负债平衡：编制初始资产负债表，确保资产总额等于负债总额加所有者权益总额
        BalanceSheet balanceSheet = generateBalanceSheet();
        // 5. 审查账务处理

        // 6. 核对财务报表
        return Result.success();
    }

    /**
     * 准备初始数据
     */
    private List<CertificateAbstract> preparingInitialData(Date now) {
        /*
         * 1. 准备初始数据：确保所有的初始数据已经录入系统，包括以下科目的初始余额：
         * a. 资产类：现金、银行存款、应收账款、存货、固定资产等。
         * b. 负债类：应付账款、预收账款、借款等。
         * c. 所有者权益类：实收资本、资本公积、未分配利润等。
         * PS：为搜索到【存货】科目，暂时忽略该科目
         */
        List<Long> initDataAccountIds = new ArrayList<>(Arrays.asList(
                CASH_ON_HAND.getId(), // 库存现金
                CASH_IN_BANK.getId(), // 银行存款
                ACCOUNT_RECEIVABLE.getId(), //应收账款
                FIXED_ASSETS.getId(), //固定资产
                ACCOUNTS_PAYABLE.getId(), //应付账款
                DEPOSIT_RECEIVED.getId(), //预收账款
                SHORT_TERM_BORROWING.getId(), //短期借款
                PAID_IN_CAPITAL.getId(), //实收资本
                UNDISTRIBUTED_PROFIT.getId() //未分配利润
        ));
        // 查询【银行存款】的全部子级科目 ID，并添加到【初始数据科目 ID】 List
        initDataAccountIds.addAll(accountService.listObjs(new LambdaQueryWrapper<Account>()
                .select(Account::getId)
                .like(Account::getParentIds, " " + CASH_IN_BANK.getId() + "")
        ));
        // 查询【实收资本】的全部子级科目 ID，并添加到【初始数据科目 ID】 List
        initDataAccountIds.addAll(accountService.listObjs(new LambdaQueryWrapper<Account>()
                .select(Account::getId)
                .like(Account::getParentIds, " " + PAID_IN_CAPITAL.getId() + "")
        ));
        return certificateAbstractService.selectJoinList(CertificateAbstract.class, new MPJLambdaWrapper<CertificateAbstract>()
                .selectAll(CertificateAbstract.class)
                // 关联科目
                .leftJoin(Account.class, Account::getId, CertificateAbstract::getAccountId, ext -> ext
                        .selectAssociation(Account.class, CertificateAbstract::getAccount)
                )
                .rightJoin(Certificate.class, Certificate::getId, CertificateAbstract::getCertificateId)
                //查询可能含有【初始金额】科目的凭证
                .in(CertificateAbstract::getAccountId, initDataAccountIds)
                //当月
                .ge(Certificate::getCreateTime, DateUtil.beginOfMonth(now))
                .lt(Certificate::getCreateTime, DateUtil.endOfMonth(now))
        );
    }

    /**
     * 编制试算平衡表
     */
    private TrialBalance generateTrialBalance(List<CertificateAbstract> initialData) {
        // 通过 Set 集合与科目的 no 字段（顶级科目的编号，相同父级科目的 no 相同）
        long countBorrowMoney = LONG_ZERO;
        long countLoansMoney = LONG_ZERO;
        Map<String, TrialBalance.Item> accountNoAndTrialBalanceItemMaps = new HashMap<>();
        for (CertificateAbstract certificateAbstract : initialData) {
            Account account = certificateAbstract.getAccount();
            String no = account.getNo();
            TrialBalance.Item trialBalanceItem;
            if(accountNoAndTrialBalanceItemMaps.containsKey(no)) {
                trialBalanceItem = accountNoAndTrialBalanceItemMaps.get(no);
                // 求和借方金额：科目借方金额 = 凭证借方金额 + 科目借方金额
                trialBalanceItem.setBorrowMoney(certificateAbstract);
                // 求和贷方金额：科目贷方金额 = 凭证贷方金额 + 科目贷方金额
                trialBalanceItem.setLoansMoney(certificateAbstract);
            } else {
                trialBalanceItem = new TrialBalance.Item(account, certificateAbstract);
                accountNoAndTrialBalanceItemMaps.put(no, trialBalanceItem);
            }
            // 求和总计借/贷方金额
            countBorrowMoney += trialBalanceItem.getBorrowMoney();
            countLoansMoney += trialBalanceItem.getLoansMoney();
        }
        boolean isBalance = countBorrowMoney == countLoansMoney;
        return new TrialBalance(new ArrayList<>(accountNoAndTrialBalanceItemMaps.values()), countBorrowMoney, countLoansMoney, isBalance);
    }

    /**
     * 核对总账与明细账
     */
    private Boolean checkGeneralLedgerAndSubsidiaryLedger() {
        // 1. 准备工作（确保所有交易已经正确地录入到系统中，并且所有的记账凭证已经审核和入账。）
        // 2. 打印或导出总账和明细账（从财务软件中导出或打印总账和各个明细账的期初余额、借方发生额、贷方发生额及期末余额）
        // 3. 核对总账与明细账的期初余额（确保总账科目的期初余额与对应明细账的期初余额之和相符。）
        // 3.1 获取当月总账
        List<CertificateAbstract> currentMonthGeneralLedger = searchAccountTree.currentMonthGeneralLedger();
        currentMonthGeneralLedger.forEach(certificateAbstract -> {

        });
        // 3.1 获取当月明细账
        List<CertificateAbstract> currentMonthSubsidiaryLedger = searchAccountTree.currentMonthSubsidiaryLedger();
        // 4. 核对总账与明细账的发生额（确保总账科目的借方和贷方发生额与其明细账的借方和贷方发生额之和相符）
        // 5. 核对总账与明细账的期末余额（确保总账科目的期末余额与对应明细账的期末余额之和相符）
        // 6. 发现并更正差异（对于发现的任何差异，查找原因并进行更正，确保总账与明细账完全一致）
        return true;
    }

    /**
     * 检查资产负债平衡
     * 编制初始资产负债表，确保资产总额等于负债总额加所有者权益总额。
     */
    private BalanceSheet generateBalanceSheet() {
        return new BalanceSheet();
    }
}
