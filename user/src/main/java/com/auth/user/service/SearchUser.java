package com.auth.user.service;

import com.auth.user.entity.User;

public interface SearchUser {

    /**
     * 通过邮箱查询
     */
    User byEmail(String email);

    /**
     * 通过邮箱查询
     * @throws IllegalArgumentException 用户 ID 不存在
     */
    User byEmailThrow(String email) throws IllegalArgumentException;

    User byPhone(String phone);

    User byId(Long id);

    User byOpenId(String openId);
}