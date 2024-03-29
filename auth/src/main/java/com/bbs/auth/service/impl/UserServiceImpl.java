package com.bbs.auth.service.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Opt;
import com.bbs.auth.app.verify.VerifyLogin;
import com.bbs.auth.dao.UserDao;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.Result;
import com.bbs.auth.entity.User;
import com.bbs.auth.entity.param.UserParam;
import com.bbs.auth.mapper.UserMapper;
import com.bbs.auth.service.UserService;
import com.bbs.entity.UserVO;
import com.bbs.enums.UserStateEnum;
import com.bbs.exception.BusinessException;
import com.bbs.exception.ReLoginException;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

import static com.bbs.Result.success;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
* @author Lenovo
* @description 针对表【user】的数据库操作Service实现
* @createDate 2023-07-11 14:46:11
*/
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserDao dao;

    @Override
    public Boolean userStateIsNormal(User user) { return UserStateEnum.STATUS_NORMAL.getCode().equals(user.getState()); }

    @Override
    public Boolean userIsValidity(User user) {
        Date expirationTime = user.getExpirationTime();
        if(nonNull(expirationTime)) {
            return DateUtil.between(
                    DateUtil.beginOfDay(expirationTime),
                    DateUtil.beginOfDay(new Date()),
                    DateUnit.DAY,
                    false
            ) < 0;
        }
        return true;
    }

    @Override
    public Boolean userIsUsable(User user) {
        return userIsValidity(user) && userStateIsNormal(user);
    }

    @Resource
    private HttpServletRequest request;

    @Resource
    @Lazy
    private VerifyLogin verifyLogin;

    @Value("${jwt.name}")
    private String tokenName;

    @Override
    public UserVO loginUser() throws ReLoginException {
        String token = request.getHeader(tokenName);
        if(StringUtils.isNotBlank(token)) {
            try {
                Result<UserVO> result = verifyLogin.verify(new VerifyLogin.UserTokenVerifyParam(token));
                if(nonNull(result.getData()))
                    return verifyLogin.verify(new VerifyLogin.UserTokenVerifyParam(token)).getData();
            } catch (InterruptedException e) {
                throw new ReLoginException();
            }
        }
        throw new ReLoginException();
    }

    @Override
    public User registerByPhoneNoLockNoLoad(Long phone) throws IllegalArgumentException {
        return registerByPhoneNoLockAndNoLoadCache(phone);
    }

    @Override
    public User registerByPhoneNoLockNoLoad(String phone) throws IllegalArgumentException {
        return registerByPhoneNoLockNoLoad(Long.valueOf(phone));
    }

    @Override
    public User registerByPhoneNoLockAndNoLoadCache(Long phone) throws IllegalArgumentException {
        User user = new User();
        user.setPhone(phone);
        user.setName(phone.toString());
        if(!save(user)) throw new BusinessException("通过手机号注册用户失败");
        return user;
    }

    @Override
    public Result<Page<User>> search(UserParam param) {
        MPJLambdaWrapper<User> wrapper = new MPJLambdaWrapper<>(User.class);
        if(nonNull(param.getId())) {
            wrapper.eq(User::getId, param.getId());
        } else {
            wrapper
                    .like(StringUtils.isNotBlank(param.getEmail()), User::getEmail, param.getEmail())
                    .likeRight(StringUtils.isNotBlank(param.getName()), User::getName, param.getName())
                    .eq(nonNull(param.getPhone()), User::getPhone, param.getPhone())
                    .between(nonNull(param.getCreateStartTime()) && nonNull(param.getCreateEndTime()), User::getCreateTime, param.getCreateStartTime(), param.getCreateEndTime())
                    .eq(nonNull(param.getStatus()), User::getState, param.getStatus())
                    .orderByDesc(isNull(param.getSortType()), User::getCreateTime)

                    .orderByDesc(nonNull(param.getSortType()) && param.getSortType() == 0, User::getExpirationTime)
                    .orderByDesc(nonNull(param.getSortType()) && param.getSortType() == 1, User::getCreateTime)
            ;
        }
        Page<User> result = wrapper.page(param.toPage());
        result.getRecords().forEach(user -> user.setStateStr(UserStateEnum.enumMap.get(user.getState()).getMsg()));
        return success(result);
    }

    @Override
    public User search(String email) {
        return lambdaQuery().eq(User::getEmail, email).one();
    }

    @Override
    public User searchByEmailElseThrow(String email) throws IllegalArgumentException {
        return Opt.ofNullable(search(email)).orElseThrow(() -> new IllegalArgumentException("对应邮箱用户不存在！"));
    }
}




