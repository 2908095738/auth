package com.bbs.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PayWay {


    WECHAT(1, "微信"),
    ALIPAY(2, "支付宝"),
    CREDIT(3, "挂账"),
    CASH(4, "现金");

    private final Integer code;

    private final String msg;

    public static String getMsgByCode(Integer code){
        for (PayWay obj : PayWay.values()) {
            if(obj.getCode().intValue() == code.intValue()){
                return obj.getMsg();
            }
        }
        return null;
    }
}
