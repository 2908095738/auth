package com.bbs.auth.app.dubbo;

import com.bbs.api.auth.User;
import com.bbs.api.auth.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
@DubboService
public class UserServiceImpl implements UserService {

    @Resource
    private UserService userService;

    @Override
    public User getLoginUser() {
        return userService.getLoginUser();
    }

    @Override
    public User getUserByToken(String token) {
        return userService.getUserByToken(token);
    }

    @Override
    public User getUserByID(Long id) {
        return userService.getUserByID(id);
    }

    @Override
    public List<User> getUserList(String token, List<Long> ids) {
        return userService.getUserList(token, ids);
    }

    @Override
    public List<User> getUserList(List<Long> ids) {
        return userService.getUserList(ids);
    }
}
