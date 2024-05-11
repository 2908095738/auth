package com.bbs.auth.app.dubbo;

import com.bbs.api.auth.User;
import com.bbs.api.auth.UserAPI;
import com.bbs.auth.converter.UserConverter;
import com.bbs.auth.service.TokenService;
import com.bbs.auth.service.UserService;
import com.bbs.entity.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
@DubboService
public class UserAPIImpl implements UserAPI {

    @Resource
    private UserService userService;

    @Resource
    private UserConverter converter;

    @Resource
    private TokenService tokenService;

    @Override
    public User getLoginUser() {
        UserVO vo = userService.loginUser();
        return converter.toAPIUser(vo);
    }

    @Override
    public User getUserByToken(String token) {
        UserVO vo = tokenService.verify(token);
        return converter.toAPIUser(vo);
    }

    @Override
    public List<User> getUserList(String token, List<Long> ids) {
        if(tokenService.verifyToken(token)) {
            return converter.toAPIUser(userService.search(ids));
        }
        return null;
    }

    @Override
    public List<User> getUserList(List<Long> ids) {
        return converter.toAPIUser(userService.search(ids));
    }
}
