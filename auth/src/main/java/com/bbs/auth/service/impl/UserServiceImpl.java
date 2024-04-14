package com.bbs.auth.service.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.crypto.SecureUtil;
import com.bbs.auth.cache.user.PhoneCache;
import com.bbs.auth.cache.user.UserCache;
import com.bbs.auth.dao.UserDao;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.auth.entity.User;
import com.bbs.auth.entity.param.UserParam;
import com.bbs.auth.mapper.UserMapper;
import com.bbs.auth.service.TokenService;
import com.bbs.auth.service.UserService;
import com.bbs.entity.UserVO;
import com.bbs.enums.UserStateEnum;
import com.bbs.exception.BusinessException;
import com.bbs.exception.ReLoginException;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.bbs.Result.success;
import static com.bbs.auth.cache.user.UserCache.cacheIsExists;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
* @author Lenovo
* @description 针对表【user】的数据库操作Service实现
* @createDate 2023-07-11 14:46:11
*/
@Slf4j
@Service
public class UserServiceImpl extends MPJBaseServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserDao db;

    @Lazy
    @Resource
    private PhoneCache phoneCache;

    @Lazy
    @Resource
    private UserCache cache;

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
    private TokenService tokenService;


    @Override
    public UserVO loginUser() throws ReLoginException {

        String token = tokenService.getToken(request);
        if(StringUtils.isNotBlank(token)) {
            try {
                return tokenService.parseToken(token);
            } catch (Exception e) {
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
    public String encryptPassword(User user) {
        return encryptPassword(user.getPassword(), user.getSalt());
    }

    @Override
    public String encryptPassword(String pwd, Integer salt) {
        return SecureUtil.md5(pwd + salt);
    }

    @Override
    public Boolean updatePasswordByID(String password, Long id) {
        return lambdaUpdate().set(User::getPassword, password).eq(User::getId, id).update();
    }

    @Override
    public User searchByPhone(String phone) {
        User user;
        Long uid = phoneCache.get(Long.valueOf(phone));
        if(cacheIsExists(uid)) {
            user = cache.get(uid);
            if(isNull(user)) {
                user = db.searchByID(uid);
            }
        } else {
            user = db.selectByPhone(phone);
        }
        return user;
    }

    @Override
    public User search(Long id) {
        User user = cache.get(id);
        if(isNull(user)) {
            user = db.searchByID(id);
        }
        return user;
    }

    @Override
    public List<User> search(List<Long> ids) {
        List<User> users = cache.get(ids);
        List<Long> cacheIsEmptyUserIds = new ArrayList<>(ids.size());
        List<Integer> cacheIsEmptyUserIndexList = new ArrayList<>(ids.size());
        for (int index = 0; index < users.size(); index++) {
            User user = users.get(index);
            if(isNull(user)) {
                cacheIsEmptyUserIds.add(ids.get(index));
                cacheIsEmptyUserIndexList.add(index);
            }
        }
        if(cacheIsEmptyUserIds.size() >= NumberUtils.INTEGER_ONE) {
            List<User> cacheIsEmptyUser = listByIds(cacheIsEmptyUserIds);
            for (int index = 0; index < cacheIsEmptyUser.size(); index++) {
                users.set(cacheIsEmptyUserIndexList.get(index), cacheIsEmptyUser.get(index));
            }
        }
        return users;
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




