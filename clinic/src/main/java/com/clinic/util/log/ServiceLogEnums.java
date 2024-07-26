package com.clinic.util.log;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ServiceLogEnums {

    STOCK("库存"),
    RETAIL("零售"),
    ADMISSION("门诊日志"),
    PAY("收费"),
    PRESCRIPTION("处方"),
    DIAGNOSIS_PROOF("诊断证明"),
    DISINFECTION("消杀记录"),
    STERILIZE("消毒记录"),
    DOSSIER("病例"),
    PATIENT("病人"),
    USER_SETTING("用户设置"),
    ;


    private final String serviceName;
}
