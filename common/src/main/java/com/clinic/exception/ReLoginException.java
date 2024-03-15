package com.clinic.exception;

import com.clinic.enums.CodeEnum;

import static com.clinic.enums.CodeEnum.*;

public class ReLoginException extends BusinessException {

    public ReLoginException() {
        super(FAILED_USER_LOGIN_EXPIRE);
    }

    public ReLoginException(CodeEnum codeEnum) {
        super(codeEnum);
    }
}
