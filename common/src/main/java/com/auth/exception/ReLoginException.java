package com.auth.exception;

import com.auth.enums.CodeEnum;

import static com.auth.enums.CodeEnum.*;

public class ReLoginException extends BusinessException {

    public ReLoginException() {
        super(FAILED_USER_LOGIN_EXPIRE);
    }

    public ReLoginException(CodeEnum codeEnum) {
        super(codeEnum);
    }
}
