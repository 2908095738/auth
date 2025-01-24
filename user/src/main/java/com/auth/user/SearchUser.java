package com.auth.user;

import com.auth.user.dto.UserDTO;

/**
 * 查询用户信息
 * @author ext.luchenlin5
 */
public interface SearchUser {

    /**
     * 通过邮箱查询
     */
    UserDTO byEmail(String email);

    /**
     * 通过邮箱查询
     * @throws IllegalArgumentException 用户 ID 不存在
     */
    UserDTO byEmailThrow(String email) throws IllegalArgumentException;

    UserDTO byPhone(String phone);

    UserDTO byId(Long id);

    UserDTO byOpenId(String openId);
}