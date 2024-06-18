package com.bbs.financial.enums;


/**
 *
 */
public enum CertificateType {

    ACCRUED_SALARY(1,"计提工资"),
    PAY_A_SALARY(2,"发放工资")
    ;

    private int key;
    private String value;

    CertificateType(int key, String value) {
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
