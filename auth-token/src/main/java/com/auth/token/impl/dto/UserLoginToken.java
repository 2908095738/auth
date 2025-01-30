package com.auth.token.impl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户登录 Token
 * @author ext.luchenlin5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginToken {

    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 签发时间
     */
    private Date IssuedDate;

    /**
     * 生效时间
     */
    private Date notBeforeDate;

    /**
     * 过期时间
     */
    private Date expireDate;

    /**
     * token
     */
    private String token;

    public UserLoginToken(Long uid, Date issuedDate, Date notBeforeDate, Date expireDate) {
        this.uid = uid;
        IssuedDate = issuedDate;
        this.notBeforeDate = notBeforeDate;
        this.expireDate = expireDate;
    }
}
