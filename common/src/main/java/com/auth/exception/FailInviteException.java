package com.auth.exception;

import com.auth.enums.CodeEnum;

import static com.auth.enums.CodeEnum.FAILED_REG_INVITE_NOT_AVAILABLE;

public class FailInviteException extends BusinessException {

    public FailInviteException() {
        super(FAILED_REG_INVITE_NOT_AVAILABLE);
    }

    public FailInviteException(CodeEnum codeEnum) {
        super(codeEnum);
    }
}