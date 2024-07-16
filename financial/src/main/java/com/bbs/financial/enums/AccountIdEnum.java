package com.bbs.financial.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccountIdEnum {

    CASH_ON_HAND(1L, "1001", "库存现金"),
    CASH_IN_BANK(2L, "1002","银行存款"),
    ACCOUNT_RECEIVABLE(11L, "1122","应收账款"),
    FIXED_ASSETS(57L, "1601","固定资产"),
    ACCOUNTS_PAYABLE(87L, "2202","应付账款"),
    DEPOSIT_RECEIVED(88L, "2203","预收账款"),
    SHORT_TERM_BORROWING(77L, "2001","短期借款"),
    PAID_IN_CAPITAL(118L, "4001","实收资本"),
    UNDISTRIBUTED_PROFIT(752L, "4104","未分配利润"),
    ;

    private final Long id;

    private final String code;

    private final String description;
}
