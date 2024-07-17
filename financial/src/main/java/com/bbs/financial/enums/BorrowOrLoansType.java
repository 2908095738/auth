package com.bbs.financial.enums;
//1借Borrow 0贷Loans
public enum BorrowOrLoansType {

    BORROW(1,"借"),
    LOANS(0,"贷"),
    FLAT(2,"平")
    ;

    private final int key;
    private final String value;

    BorrowOrLoansType(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public int getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }



}
