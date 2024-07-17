package com.bbs.financial.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.financial.entity.Account;
import com.bbs.financial.entity.LedgerGeneral;
import com.bbs.financial.enums.AccountAbstractEnum;
import com.bbs.financial.enums.BorrowOrLoansType;
import com.bbs.financial.exception.DataMissingException;
import com.bbs.financial.mapper.LedgerGeneralMapper;
import com.bbs.financial.service.LedgerGeneralService;
import com.bbs.financial.util.LoginUser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    @Override
    public List<LedgerGeneral> searchByAccountId(List<Long> accountIds, AccountAbstractEnum abstractEnum) {
//        return lambdaQuery()
//                ;
        return null;
    }

    @Override
    public void tryInitCurrentPeriodAccountLedgerGeneral(Date date, Account account) {
        // 指定月，指定科目的总账记录（可能含年初余额、期初余额、本期合计、本年累计）
        LedgerGeneral currentMonthAccountLedgerGeneralList = lambdaQuery()
                .ge(LedgerGeneral::getCreateTime, DateUtil.beginOfMonth(date))
                .lt(LedgerGeneral::getCreateTime, DateUtil.endOfMonth(date))
                .eq(LedgerGeneral::getAccountingSetId, LoginUser.getLoginSetId())
                .eq(LedgerGeneral::getCertificateAbstract, CURRENT_TOTAL.getName())
                .one();
        List<LedgerGeneral> needSaveLedgerGeneral = new ArrayList<>();

        LedgerGeneral yearBeginningBalance = searchBeginningBalance(date);
        // 没有【年初余额】就初始化
        if(isNull(yearBeginningBalance)) {
            List<LedgerGeneral> allCurrentYearCumulative = lambdaQuery()
                    .eq(LedgerGeneral::getAccountingSetId, LoginUser.getLoginSetId())
                    .eq(LedgerGeneral::getCertificateAbstract, CURRENT_YEAR_CUMULATIVE.getName())
                    .list();
            LedgerGeneral ledgerGeneral;
            if(allCurrentYearCumulative.size() > INTEGER_ZERO) {
                // 将最后有【本年累计】的一年，的数据作为当前年的【年初余额】使用
                LedgerGeneral lastCurrentYearCumulative = allCurrentYearCumulative.get(allCurrentYearCumulative.size() - INTEGER_ONE);

                ledgerGeneral = new LedgerGeneral();
                BeanUtil.copyProperties(lastCurrentYearCumulative, ledgerGeneral, true);
                ledgerGeneral.setId(null);
                ledgerGeneral.setCertificateAbstract(BEGINNING_BALANCE.getName());
                ledgerGeneral.setCreateTime(null);
                ledgerGeneral.setCreateBy(LoginUser.getId());
                ledgerGeneral.setUpdateTime(null);
                ledgerGeneral.setUpdateBy(null);

                ledgerGeneral = lastCurrentYearCumulative;
                // 补全【缺失数据】的年份的数据
                for (int year = DateUtil.year(lastCurrentYearCumulative.getCreateTime()); year < DateUtil.year(date); year++) {
                    tryInitCurrentPeriodAccountLedgerGeneral(DateUtil.parse(year + "-01-01", "yyyy-MM-dd"), account);
                }
            } else {
                ledgerGeneral = new LedgerGeneral(account, BEGINNING_BALANCE.getName(), LONG_ZERO, LONG_ZERO, BorrowOrLoansType.FLAT.getKey(), LONG_ZERO);
            }
            needSaveLedgerGeneral.add(ledgerGeneral);
        }
        LedgerGeneral beginningBalance = searchOpeningBalance(date);
        // 没有【期初余额】就初始化
        if(isNull(beginningBalance)) {
            needSaveLedgerGeneral.add(new LedgerGeneral(account, CURRENT_TOTAL.getName(), LONG_ZERO, LONG_ZERO, BorrowOrLoansType.FLAT.getKey(), LONG_ZERO));
        }
        LedgerGeneral currentTotal = searchCurrentTotal(date);
        // 没有【本期合计】就初始化
        if(isNull(currentTotal)) {
            needSaveLedgerGeneral.add(new LedgerGeneral(account, CURRENT_TOTAL.getName(), LONG_ZERO, LONG_ZERO, BorrowOrLoansType.FLAT.getKey(), LONG_ZERO));
        }
        // 没有【本年累计】就初始化
        LedgerGeneral currentYearCumulative = searchCurrentYearCumulative(date);
        if(isNull(currentYearCumulative)) {
            needSaveLedgerGeneral.add(new LedgerGeneral(account, CURRENT_TOTAL.getName(), LONG_ZERO, LONG_ZERO, BorrowOrLoansType.FLAT.getKey(), LONG_ZERO));
        }
        saveBatch(needSaveLedgerGeneral);
    }

    @Override
    public LedgerGeneral tryInitBeginningBalance(Date date, Account account, Boolean useOldData) throws DataMissingException {
        // 1. 查询指定日期的【年初余额】
        LedgerGeneral beginningBalance = searchBeginningBalance(date);
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
        LedgerGeneral openingBalance = searchOpeningBalance(date);
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

    /**
     * 年初余额
     */
    private LedgerGeneral searchBeginningBalance(Date date) {
        return lambdaQuery()
                .ge(LedgerGeneral::getCreateTime, DateUtil.beginOfYear(date))
                .lt(LedgerGeneral::getCreateTime, DateUtil.endOfYear(date))
                .eq(LedgerGeneral::getAccountingSetId, LoginUser.getLoginSetId())
                .eq(LedgerGeneral::getCertificateAbstract, BEGINNING_BALANCE.getName())
                .one();
    }

    /**
     * 期初余额
     */
    private LedgerGeneral searchOpeningBalance(Date date) {
        return lambdaQuery()
                .ge(LedgerGeneral::getCreateTime, DateUtil.beginOfMonth(date))
                .lt(LedgerGeneral::getCreateTime, DateUtil.endOfMonth(date))
                .eq(LedgerGeneral::getAccountingSetId, LoginUser.getLoginSetId())
                .eq(LedgerGeneral::getCertificateAbstract, OPENING_BALANCE.getName())
                .one();
    }

    /**
     * 本期合计
     */
    private LedgerGeneral searchCurrentTotal(Date date) {
        return lambdaQuery()
                .ge(LedgerGeneral::getCreateTime, DateUtil.beginOfMonth(date))
                .lt(LedgerGeneral::getCreateTime, DateUtil.endOfMonth(date))
                .eq(LedgerGeneral::getAccountingSetId, LoginUser.getLoginSetId())
                .eq(LedgerGeneral::getCertificateAbstract, CURRENT_TOTAL.getName())
                .one();
    }

    /**
     * 本年累计
     */
    private LedgerGeneral searchCurrentYearCumulative(Date date) {
        return lambdaQuery()
                .ge(LedgerGeneral::getCreateTime, DateUtil.beginOfYear(date))
                .lt(LedgerGeneral::getCreateTime, DateUtil.endOfYear(date))
                .eq(LedgerGeneral::getAccountingSetId, LoginUser.getLoginSetId())
                .eq(LedgerGeneral::getCertificateAbstract, CURRENT_YEAR_CUMULATIVE.getName())
                .one();
    }
}




