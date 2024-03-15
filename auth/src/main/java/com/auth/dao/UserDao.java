package com.auth.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.auth.entity.User;
import com.auth.mapper.UserMapper;
import org.springframework.stereotype.Component;

import static java.util.Objects.nonNull;

@Component
public class UserDao extends ServiceImpl<UserMapper, User> {
    /**
     * 验证手机号是否注册过
     * @param user 验证参数
     * @return 手机号是否注册过
     * 手机号校验方式：nonNull(phone) && phone.toString().length() <= 11, User::getPhone, phone)
     */
    public boolean userIsExist(User user){
        return lambdaQuery()
                .eq(User::getName, user.getName())
                .or()
                .eq(User::getEmail, user.getEmail())
                .exists();
    }

    /**
     * 根据手机号查询加过密的密码
     * @param phone 入参
     * @return User
     */
    public User selectByPhone(String phone){
        return lambdaQuery().eq(nonNull(phone),User::getPhone,phone).one();
    }

    /**
     * 根据用户id查询用户信息
     * @param userId 用户Id
     * @return User
     */
    public User selectByUserId(Long userId){
        return lambdaQuery().eq(nonNull(userId), User::getId,userId).one();
    }

    public User selectByEmail(String email) { return lambdaQuery().eq(User::getEmail, email).one(); }
}
