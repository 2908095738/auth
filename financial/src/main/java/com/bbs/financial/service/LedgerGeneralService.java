package com.bbs.financial.service;

import com.bbs.financial.entity.Account;
import com.bbs.financial.entity.LedgerGeneral;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.financial.exception.DataMissingException;
import com.bbs.financial.service.impl.LedgerGeneralServiceImpl;

import java.util.Date;
import java.util.List;

/**
* @author ludada
* @description 针对表【ledger_general(总账)】的数据库操作Service
* @createDate 2024-07-16 11:08:04
*/
public interface LedgerGeneralService extends IService<LedgerGeneral> {

    /**
     * 尝试初始化并获取指定科目、指定会计期间的总账数据（包含年初余额、期初余额、本期合计、本年累计）
     * @param date 指定会计期间
     * @param account 指定科目
     * @param useOldData 是否使用旧数据（应用于年初余额、期初余额，如果上一期没有如本期合计、本年累计的数据，是否使用最近有数据的一期）
     * @return { 年初余额, 期初余额, 本期合计, 本年累计 }
     */
    LedgerGeneralServiceImpl.AccountLedgerGeneral tryInitAccountLedgerGeneral(Date date, Account account, Boolean useOldData);

    /**
     * 尝试初始化并获取指定科目、会计期间的【年初余额】
     */
    LedgerGeneral tryInitBeginningBalance(Date date, Account account, Boolean useOldData) throws DataMissingException;


    /**
     * 尝试初始化并获取指定科目、会计期间的【期初余额】
     */
    LedgerGeneral tryInitOpeningBalance(Date date, Account account, Boolean useOldData) throws DataMissingException;


    /**
     * 尝试初始化并获取指定科目、会计期间的【本期合计】
     */
    LedgerGeneral tryInitCurrentTotal(Date date, Account account);

    /**
     * 尝试初始化并获取指定科目、会计期间的【本年累计】
     */
    LedgerGeneral tryInitCurrentYearCumulative(Date date, Account account);

    /**
     * 查询【年初余额】
     */
    LedgerGeneral searchBeginningBalance(Date date, Account account);

    /**
     * 查询【期初余额】
     */
    LedgerGeneral searchOpeningBalance(Date date, Account account);

    /**
     * 查询【本期合计】
     */
    LedgerGeneral searchCurrentTotal(Date date, Account account);


    List<LedgerGeneral> searchCurrentTotal(Date date, List<Long> accountIds);

    /**
     * 查询【本年累计】
     */
    LedgerGeneral searchCurrentYearCumulative(Date date, Account account);

    /**
     * 更新科目【本期合计】
     */
    List<LedgerGeneral> computeAccountCurrentTotal(Date date, Account account, LedgerGeneral ledgerGeneral, Long addBorrowMoney, Long addLoansMoney);
}
