package com.auth.dao;

import com.auth.entity.UserBind;
import com.auth.mapper.UserBindMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.auth.entity.User;
import com.auth.mapper.UserMapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static java.util.Objects.nonNull;

@Component
public class UserDao extends ServiceImpl<UserMapper, User> {

    @Resource
    private UserBindMapper userBindMapper;

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

    public UserBind searchUserBind(String openID) {
        return userBindMapper.selectJoinOne(UserBind.class, new MPJLambdaWrapper<UserBind>()
                .selectAll(UserBind.class)
                .selectAssociation(User.class, UserBind::getUser)
                .leftJoin(User.class, User::getId, UserBind::getUserId)
                .eq(UserBind::getOpenId, openID)
                .eq(UserBind::getState, NumberUtils.INTEGER_ZERO));
    }

    /**
     * 根据手机号查询加过密的密码
     * @param phone 入参
     * @return User
     */
    public User selectByPhone(String phone){
        return lambdaQuery().eq(nonNull(phone),User::getPhone,phone).one();
    }

    public User selectByPhone(Integer phone){
        return selectByPhone(phone.toString());
    }
}
