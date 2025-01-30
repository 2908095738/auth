package com.auth.login;

import com.auth.cache.login.fail.count.LoginFailCount;
import com.auth.login.param.Param;
import com.auth.phone.cache.PhoneUserIdCache;
import com.auth.phone.code.cache.PhoneCodeCache;
import com.auth.user.User;
import com.auth.user.cache.UserCache;
import com.auth.user.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

import static java.util.Objects.nonNull;

/**
 * 登录策略抽象类
 * 策略模式：登录场景下，使用不同登录类型实现
 * @author luchenlin
 */
@Slf4j
public abstract class AbstractLoginStrategy {

    @Resource
    protected PhoneUserIdCache phoneUserIdCache;

    @Resource
    protected UserCache userCache;

    @Resource
    protected User.Search searchUser;

    @Resource
    protected User.Edit editUser;

    @Resource
    protected User.Save saveUser;

    @Resource
    protected PhoneCodeCache phoneCodeCache;

    @Resource
    protected LoginFailCount loginFailCount;


    protected abstract String getLoginTypeCode();

    protected abstract void checkParam(Param param) throws IllegalArgumentException;

    protected abstract UserDTO login(Param param) throws IllegalArgumentException;

    public UserDTO tryLogin(Param param) {

        checkParam(param);

        return login(param);
    }

    protected UserDTO searchUser(String phone) {
        Long uid = phoneUserIdCache.get(phone);
        UserDTO user;
        if(nonNull(uid)) {
            user = userCache.get(uid);
            if(nonNull(user)) {
                return user;
            }
        }
        user = searchUser.byPhone(phone);
        phoneUserIdCache.reload(phone, user.getId());
        userCache.reload(user);
        return user;
    }
}
