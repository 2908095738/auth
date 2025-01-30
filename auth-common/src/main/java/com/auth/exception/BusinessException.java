package com.auth.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {

    private Integer code;

    private String msg;

    public BusinessException(String msg) {
        code = 500;
        this.msg = msg;
    }

    public static void throwException(String msg) throws BusinessException {
        throw new BusinessException(401, msg);
    }
}
