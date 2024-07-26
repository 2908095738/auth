package com.clinic.util.log;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ServiceLogEnums {

    STOCK(1, "库存"),
    RETAIL(2, "零售"),
    ADMISSION(3, "门诊日志"),
    PAY(4, "收费"),
    PRESCRIPTION(5, "处方"),
    DIAGNOSIS_PROOF(6, "诊断证明"),
    DISINFECTION(7, "消杀记录"),
    STERILIZE(8, "消毒记录"),
    DOSSIER(9, "病例"),
    PATIENT(10, "病人"),
    USER_SETTING(11, "用户设置"),
    ;

    private final Integer serviceCode;


    private final String serviceName;
}
