package com.auth.user.service.impl;

import com.auth.user.entity.User;
import com.auth.user.service.SearchUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SearchUserImpl implements SearchUser {
    @Override
    public User byEmail(String email) {
        return null;
    }

    @Override
    public User byEmailThrow(String email) throws IllegalArgumentException {
        return null;
    }

    @Override
    public User byPhone(String phone) {
        return null;
    }

    @Override
    public User byId(Long id) {
        return null;
    }

    @Override
    public User byOpenId(String openId) {
        return null;
    }
}
