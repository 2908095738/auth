package com.auth.token.impl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录 Token
 * @author ext.luchenlin5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginToken {

    private Long userId;

    private String userName;

    private Long timeout;
}
