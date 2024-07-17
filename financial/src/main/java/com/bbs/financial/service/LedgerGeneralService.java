package com.bbs.financial.service;

import com.bbs.financial.entity.Account;
import com.bbs.financial.entity.LedgerGeneral;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.financial.enums.AccountAbstractEnum;
import com.bbs.financial.exception.DataMissingException;

import java.util.Date;
import java.util.List;

/**
* @author ludada
* @description 针对表【ledger_general(总账)】的数据库操作Service
* @createDate 2024-07-16 11:08:04
*/
public interface LedgerGeneralService extends IService<LedgerGeneral> {

    List<LedgerGeneral> searchByAccountId(List<Long> accountIds, AccountAbstractEnum abstractEnum);

    void tryInitCurrentPeriodAccountLedgerGeneral(Date date, Account account);

    /**
     * 尝试初始化并获取指定科目、会计期间的年初余额
     */
    LedgerGeneral tryInitBeginningBalance(Date date, Account account, Boolean useOldData) throws DataMissingException;


    /**
     * 尝试初始化并获取指定科目、会计期间的期初余额
     */
    LedgerGeneral tryInitOpeningBalance(Date date, Account account, Boolean useOldData) throws DataMissingException;
}
