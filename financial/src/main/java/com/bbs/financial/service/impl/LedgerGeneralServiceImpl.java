package com.bbs.financial.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.financial.entity.Account;
import com.bbs.financial.entity.LedgerGeneral;
import com.bbs.financial.enums.BorrowOrLoansType;
import com.bbs.financial.exception.DataMissingException;
import com.bbs.financial.mapper.LedgerGeneralMapper;
import com.bbs.financial.service.LedgerGeneralService;
import com.bbs.financial.util.LoginUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import static com.bbs.financial.enums.AccountAbstractEnum.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.*;

/**
* @author ludada
* @description 针对表【ledger_general(总账)】的数据库操作Service实现
* @createDate 2024-07-16 11:08:04
*/
@Service
public class LedgerGeneralServiceImpl extends ServiceImpl<LedgerGeneralMapper, LedgerGeneral>
    implements LedgerGeneralService{

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountLedgerGeneral {

        /**
         * 年初余额
         */
        private LedgerGeneral beginningBalance;

        /**
         * 期初余额
         */
        private LedgerGeneral openingBalance;

        /**
         * 本期合计
         */
        private LedgerGeneral currentTotal;

        /**
         * 本年累计
         */
        private LedgerGeneral currentYearCumulative;
    }

    @Override
    public AccountLedgerGeneral tryInitAccountLedgerGeneral(Date date, Account account, Boolean useOldData) {
        LedgerGeneral beginningBalance = tryInitBeginningBalance(date, account, useOldData);                    //年初余额
        LedgerGeneral openingBalance = tryInitOpeningBalance(date, account, useOldData);                        //期初余额
        LedgerGeneral currentTotal = tryInitCurrentTotal(date, account);                                        //本期合计
        LedgerGeneral currentYearCumulative = tryInitCurrentYearCumulative(date, account);                      //本年累计
        return new AccountLedgerGeneral(beginningBalance, openingBalance, currentTotal, currentYearCumulative);
    }

    @Override
    public LedgerGeneral tryInitBeginningBalance(Date date, Account account, Boolean useOldData) throws DataMissingException {
        // 1. 查询指定日期的【年初余额】
        LedgerGeneral beginningBalance = searchBeginningBalance(date, account);
        int lastYear = (DateUtil.year(date) - INTEGER_ONE);
        DateTime lastYearDateTime = DateUtil.parse((DateUtil.year(date) - INTEGER_ONE) + "-01-01", "yyyy-MM-dd");
        // 2. 如果不存在【年初余额】
        if(isNull(beginningBalance)) {
            // 2.1 则查询上一年的【本年累计】
            LedgerGeneral lastYearCurrentYearCumulative = lambdaQuery()
                    .ge(LedgerGeneral::getCreateTime, lastYearDateTime)
                    .lt(LedgerGeneral::getCreateTime, DateUtil.endOfYear(lastYearDateTime))
                    .eq(LedgerGeneral::getAccountingSetId, LoginUser.getLoginSetId())
                    .eq(LedgerGeneral::getCertificateAbstract, CURRENT_YEAR_CUMULATIVE.getName())
                    .one();
            // 2.2 如果上一年存在【本年累计】
            if (nonNull(lastYearCurrentYearCumulative)) {
                // 2.2.1 则使用上一年的【本年累计】作为本年【年初余额】
                beginningBalance = new LedgerGeneral();
                BeanUtil.copyProperties(lastYearCurrentYearCumulative, beginningBalance, true);
                beginningBalance.setId(null);
                beginningBalance.setCertificateAbstract(BEGINNING_BALANCE.getName());
                beginningBalance.setCreateTime(null);
                beginningBalance.setCreateBy(LoginUser.getId());
                beginningBalance.setUpdateTime(null);
                beginningBalance.setUpdateBy(null);
                beginningBalance.insert();
            } else {
                // 2.2.2 否则，查看是否使用旧数据
                if (useOldData) {
                    // 2.2.2.1 如果使用旧数据，则使用最近年的【本年累计】作为本年【年初余额】
                    List<LedgerGeneral> allCurrentYearCumulative = lambdaQuery()    //查询历史【本年累计】
                            .eq(LedgerGeneral::getAccountingSetId, LoginUser.getLoginSetId())
                            .eq(LedgerGeneral::getCertificateAbstract, CURRENT_YEAR_CUMULATIVE.getName())
                            .list();
                    if (allCurrentYearCumulative.size() > INTEGER_ZERO) {
                        LedgerGeneral lastCurrentYearCumulative = allCurrentYearCumulative.get(allCurrentYearCumulative.size() - INTEGER_ONE);
                        beginningBalance = new LedgerGeneral();
                        BeanUtil.copyProperties(lastCurrentYearCumulative, beginningBalance, true);
                        beginningBalance.setId(null);
                        beginningBalance.setCertificateAbstract(BEGINNING_BALANCE.getName());
                        beginningBalance.setCreateTime(null);
                        beginningBalance.setCreateBy(LoginUser.getId());
                        beginningBalance.setUpdateTime(null);
                        beginningBalance.setUpdateBy(null);
                        beginningBalance.insert();
                    }
                }
                // 否则抛出异常
                DataMissingException.throwException(StrUtil.format("科目【{}】缺少 {} 年（上一年）的【本年累计】数据", account.getId(), lastYear));
            }
        }
        return beginningBalance;
    }

    @Override
    public LedgerGeneral tryInitOpeningBalance(Date date, Account account, Boolean useOldData) throws DataMissingException {
        // 1. 查询指定日期的【期初余额】
        LedgerGeneral openingBalance = searchOpeningBalance(date, account);
        // 2. 如果不存在【期初余额】
        if(isNull(openingBalance)) {
            // 2.1 则查询上一月的【本期合计】
            DateTime lastMonth = DateUtil.lastMonth();
            LedgerGeneral lastMonthOpeningBalance = lambdaQuery()
                    .ge(LedgerGeneral::getCreateTime, DateUtil.beginOfMonth(lastMonth))
                    .lt(LedgerGeneral::getCreateTime, DateUtil.endOfYear(lastMonth))
                    .eq(LedgerGeneral::getAccountingSetId, LoginUser.getLoginSetId())
                    .eq(LedgerGeneral::getCertificateAbstract, CURRENT_TOTAL.getName())
                    .one();
            // 2.2 如果上一月存在【本期合计】
            if (nonNull(lastMonthOpeningBalance)) {
                // 2.2.1 则使用上一月的【本期合计】作为本月【期初余额】
                openingBalance = new LedgerGeneral();
                BeanUtil.copyProperties(lastMonthOpeningBalance, openingBalance, true);
                openingBalance.setId(null);
                openingBalance.setCertificateAbstract(OPENING_BALANCE.getName());
                openingBalance.setCreateTime(null);
                openingBalance.setCreateBy(LoginUser.getId());
                openingBalance.setUpdateTime(null);
                openingBalance.setUpdateBy(null);
                openingBalance.insert();
            } else {
                // 2.2.2 否则，查看是否使用旧数据
                if (useOldData) {
                    // 2.2.2.1 如果使用旧数据，则使用最近年的【本年累计】作为本年【年初余额】
                    List<LedgerGeneral> allOpeningBalance = lambdaQuery()    //查询历史【本年累计】
                            .eq(LedgerGeneral::getAccountingSetId, LoginUser.getLoginSetId())
                            .eq(LedgerGeneral::getCertificateAbstract, CURRENT_TOTAL.getName())
                            .list();
                    if (allOpeningBalance.size() > INTEGER_ZERO) {
                        LedgerGeneral lastOpeningBalance = allOpeningBalance.get(allOpeningBalance.size() - INTEGER_ONE);
                        openingBalance = new LedgerGeneral();
                        BeanUtil.copyProperties(lastOpeningBalance, openingBalance, true);
                        openingBalance.setId(null);
                        openingBalance.setCertificateAbstract(OPENING_BALANCE.getName());
                        openingBalance.setCreateTime(null);
                        openingBalance.setCreateBy(LoginUser.getId());
                        openingBalance.setUpdateTime(null);
                        openingBalance.setUpdateBy(null);
                        openingBalance.insert();
                    }
                }
                // 否则抛出异常
                DataMissingException.throwException(StrUtil.format("科目【{}】缺少 {} 月（上个月）的【本年累计】数据", account.getId(), DateUtil.lastMonth()));
            }
        }
        return openingBalance;
    }

    @Override
    public LedgerGeneral tryInitCurrentTotal(Date date, Account account) {
        LedgerGeneral ledgerGeneral = searchCurrentTotal(date, account);
        if(isNull(ledgerGeneral)) {
            ledgerGeneral = new LedgerGeneral(account, CURRENT_TOTAL.getName(), LONG_ZERO, LONG_ZERO, BorrowOrLoansType.FLAT.getKey(), LONG_ZERO);
            ledgerGeneral.insert();
        }
        return ledgerGeneral;
    }

    @Override
    public LedgerGeneral tryInitCurrentYearCumulative(Date date, Account account) {
        LedgerGeneral ledgerGeneral = searchCurrentYearCumulative(date, account);
        if(isNull(ledgerGeneral)) {
            ledgerGeneral = new LedgerGeneral(account, CURRENT_YEAR_CUMULATIVE.getName(), LONG_ZERO, LONG_ZERO, BorrowOrLoansType.FLAT.getKey(), LONG_ZERO);
            ledgerGeneral.insert();
        }
        return ledgerGeneral;
    }

    /**
     * 年初余额
     */
    @Override
    public LedgerGeneral searchBeginningBalance(Date date, Account account) {
        return lambdaQuery()
                .ge(LedgerGeneral::getCreateTime, DateUtil.beginOfYear(date))
                .lt(LedgerGeneral::getCreateTime, DateUtil.endOfYear(date))
                .eq(LedgerGeneral::getAccountingSetId, LoginUser.getLoginSetId())
                .eq(LedgerGeneral::getCertificateAbstract, BEGINNING_BALANCE.getName())
                .eq(LedgerGeneral::getAccountId, account.getId())
                .one();
    }

    /**
     * 期初余额
     */
    @Override
    public LedgerGeneral searchOpeningBalance(Date date, Account account) {
        return lambdaQuery()
                .ge(LedgerGeneral::getCreateTime, DateUtil.beginOfMonth(date))
                .lt(LedgerGeneral::getCreateTime, DateUtil.endOfMonth(date))
                .eq(LedgerGeneral::getAccountingSetId, LoginUser.getLoginSetId())
                .eq(LedgerGeneral::getCertificateAbstract, OPENING_BALANCE.getName())
                .eq(LedgerGeneral::getAccountId, account.getId())
                .one();
    }

    /**
     * 本期合计
     */
    @Override
    public LedgerGeneral searchCurrentTotal(Date date, Account account) {
        return lambdaQuery()
                .ge(LedgerGeneral::getCreateTime, DateUtil.beginOfMonth(date))
                .lt(LedgerGeneral::getCreateTime, DateUtil.endOfMonth(date))
                .eq(LedgerGeneral::getAccountingSetId, LoginUser.getLoginSetId())
                .eq(LedgerGeneral::getCertificateAbstract, CURRENT_TOTAL.getName())
                .eq(LedgerGeneral::getAccountId, account.getId())
                .one();
    }

    @Override
    public List<LedgerGeneral> searchCurrentTotal(Date date, List<Long> accountIds) {
        return lambdaQuery()
                .ge(LedgerGeneral::getCreateTime, DateUtil.beginOfMonth(date))
                .lt(LedgerGeneral::getCreateTime, DateUtil.endOfMonth(date))
                .eq(LedgerGeneral::getAccountingSetId, LoginUser.getLoginSetId())
                .eq(LedgerGeneral::getCertificateAbstract, CURRENT_TOTAL.getName())
                .in(LedgerGeneral::getAccountId, accountIds)
                .list();
    }

    /**
     * 本年累计
     */
    @Override
    public LedgerGeneral searchCurrentYearCumulative(Date date, Account account) {
        return lambdaQuery()
                .ge(LedgerGeneral::getCreateTime, DateUtil.beginOfYear(date))
                .lt(LedgerGeneral::getCreateTime, DateUtil.endOfYear(date))
                .eq(LedgerGeneral::getAccountingSetId, LoginUser.getLoginSetId())
                .eq(LedgerGeneral::getCertificateAbstract, CURRENT_YEAR_CUMULATIVE.getName())
                .eq(LedgerGeneral::getAccountId, account.getId())
                .one();
    }

    @Override
    public List<LedgerGeneral> computeAccountCurrentTotal(Date date, Account account, LedgerGeneral currentAccountLedgerGeneral, Long addBorrowMoney, Long addLoansMoney) {
        // 更新当前科目【本期合计】
        currentAccountLedgerGeneral.computeAccountBalance(addBorrowMoney, addLoansMoney);
        // 更新父级科目【本期合计】
        List<LedgerGeneral> parentAccountLedgerGenerals = searchCurrentTotal(date, Account.getParents(account));
        for (LedgerGeneral parentAccountLedgerGeneral: parentAccountLedgerGenerals) {
            parentAccountLedgerGeneral.computeAccountBalance(addBorrowMoney, addLoansMoney);
        }
        parentAccountLedgerGenerals.add(currentAccountLedgerGeneral);
        return parentAccountLedgerGenerals;
    }
}




