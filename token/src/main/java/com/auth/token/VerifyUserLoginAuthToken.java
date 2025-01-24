package com.auth.token;

import com.auth.token.impl.exception.UserTokenParseException;

/**
 * 验证 Token
 * @author ext.luchenlin5
 */
public interface VerifyUserLoginAuthToken {

    Boolean verify(String token) throws UserTokenParseException;
}
