package com.bbs.api.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    /**
     * 头像 URL 地址
     */
    private String avatar;

    private String name;

    private String email;

    private String phone;

    private String token;

    private Long failureTokenTime;

    /**
     * 是否关注了当前登录用户
     */
    private Boolean isFollow;
}
