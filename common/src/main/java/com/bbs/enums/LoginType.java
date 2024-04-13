package com.bbs.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static java.util.Objects.nonNull;

@Getter
@AllArgsConstructor
public enum LoginType {

    PHONE(0, "手机号"),
    PASSWORD(1, "密码"),
    WX(2, "微信");

    private final Integer code;

    private final String msg;

    /**
     * 校验格式是否正确（注意！在调整该类后，检查该方法是否受影响）
     * @param type LoginType
     * @return 格式是否正确
     */
    public static Boolean checkFormat(Integer type) {
        return nonNull(type) && type >= 0 && type <= 2;
    }
}
