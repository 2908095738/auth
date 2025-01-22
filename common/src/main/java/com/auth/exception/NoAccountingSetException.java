package com.auth.exception;

import com.auth.enums.CodeEnum;

import static com.auth.enums.CodeEnum.FAILED_USER_LOGIN_EXPIRE;

public class NoAccountingSetException extends BusinessException {

    public NoAccountingSetException() {
        super(FAILED_USER_LOGIN_EXPIRE);
    }

    public NoAccountingSetException(CodeEnum codeEnum) {
        super(codeEnum);
    }
}
