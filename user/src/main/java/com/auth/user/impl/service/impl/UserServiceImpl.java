package com.auth.user.impl.service.impl;

import com.auth.user.EditUser;
import com.auth.user.dto.UserDTO;
import com.auth.user.entity.User;
import com.auth.user.SearchUser;
import com.auth.user.impl.mapper.UserMapper;
import com.github.yulichang.base.MPJBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.auth.user.impl.converter.UserConverter.CONVERTER;

/**
 * 查询用户
 * @author ext.luchenlin5
 */
@Slf4j
@Service
public class UserServiceImpl extends MPJBaseServiceImpl<UserMapper, User> implements SearchUser, EditUser {

    @Override
    public UserDTO byEmail(String email) {
        return null;
    }

    @Override
    public UserDTO byEmailThrow(String email) throws IllegalArgumentException {
        return null;
    }

    @Override
    public UserDTO byPhone(String phone) {
        return null;
    }
    @Override
    public UserDTO byId(Long id) {
        User entity = getById(id);
        return CONVERTER.toDTO(entity);
    }

    @Override
    public UserDTO byOpenId(String openId) {
        return null;
    }

    @Override
    public void updateAvatar(Long userId, String avatar) {
        lambdaUpdate().set(User::getAvatar, avatar).eq(User::getId, userId).update();
    }
}
