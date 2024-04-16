package com.bbs.auth.app.login;

import cn.hutool.json.JSONUtil;
import com.bbs.auth.cache.user.UserCache;
import com.bbs.auth.dao.UserDao;
import com.bbs.auth.service.UserService;
import com.bbs.auth.util.RedisUtil;
import com.bbs.auth.util.ZKUtil;
import com.bbs.Result;
import com.bbs.auth.cache.code.PhoneCodeCache;
import com.bbs.auth.entity.User;
import com.bbs.enums.LoginType;
import com.bbs.enums.UserStateEnum;
import com.bbs.auth.enums.ZookeeperNodePaths;
import com.bbs.auth.service.TokenService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import static com.bbs.Result.failed;
import static com.bbs.Result.success;
import static com.bbs.auth.util.PhoneUtil.checkPhoneCodeFormat;
import static com.bbs.auth.util.PhoneUtil.checkPhoneFormat;
import static com.bbs.auth.enums.RedisKeys.USER_LOGIN_PHONE;
import static com.bbs.enums.CodeEnum.*;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Slf4j
@RestController
@RequestMapping
public class Login {

    @Resource
    private UserCache userCache;
    @Resource
    private UserService service;
    @Resource
    private PhoneCodeCache phoneCodeCache;

    @Resource
    private TokenService tokenService;

    @Resource
    private RedisUtil.Redisson redissonUtil;

    @Resource
    private RedissonClient redisson;

    @Resource
    private ZKUtil zkUtil;

    @Resource
    private UserDao db;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {

        /**
         * 手机号
         */
        @NotBlank
        @Length(max = 11, min = 11, message = "手机号格式错误")
        private String phone;

        /**
         * 验证码
         */
        private String code;

        private String password;

        /**
         * 登录类型
         */
        private Integer type;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VO {
        /**
         * 用户ID
         */
        private Long uid;
        /**
         * 用户名称
         */
        private String name;

        private String token;
    }

    @PostMapping("/login")
    public Result<VO> login(@Valid @RequestBody Param param) throws InterruptedException, IllegalArgumentException {
        String phone = param.getPhone();
        return redissonUtil.lockExec(
            () -> {
                log.debug("[Login::login] param={}", JSONUtil.toJsonPrettyStr(param));
                User user;
                checkArgument(LoginType.checkFormat(param.type), FAILED_LOGIN_TYPE_NOT_AVAILABLE);
                if(LoginType.PHONE.getCode().equals(param.type)) {
                    checkPhoneFormat(phone);
                    checkPhoneCodeFormat(param.code);
                    Integer code = phoneCodeCache.getCode(phone);
                    checkArgument(nonNull(code) && code.equals(Integer.valueOf(param.code)), FAILED_AUTH_PHONE_CODE_NOT_AVAILABLE);
                    phoneCodeCache.delCode(phone);
                    user = userCache.searchByPhoneNoLockNoLoad(phone);
                    checkArgument(nonNull(user), FAILED_LOGIN_USER_NOT_EXISTS);
                    checkArgument(UserStateEnum.STATUS_NORMAL.getCode().equals(user.getState()), FAILED_LOGIN_USER_STATUS_ERROR);
                } else if (LoginType.WX.getCode().equals(param.type)) {
                    throw new IllegalArgumentException("微信登录未开通");
                } else {
                    checkPhoneFormat(phone);
                    checkArgument(StringUtils.isNoneBlank(param.password));
                    user = userCache.searchByPhoneNoLockNoLoad(phone);
                    if(isNull(user)) user = db.selectByPhone(param.getPhone());
                    checkArgument(nonNull(user), FAILED_LOGIN_USER_NOT_EXISTS);
                    checkArgument(UserStateEnum.STATUS_NORMAL.getCode().equals(user.getState()), FAILED_LOGIN_USER_STATUS_ERROR);
                    String encryptPassword = service.encryptPassword(param.password, user.getSalt());
                    checkArgument(user.getPassword().equals(encryptPassword), FAILED_LOGIN_PWD_ERROR);
                }
                String token = tokenService.createToken(user);
                tokenService.setLoginFlag(user.getId());
                userCache.expireUserAndPhoneMap(user);
                log.debug("[Login::login] 用户登录 user={}; token={}", JSONUtil.toJsonPrettyStr(user), token);
                return success(new VO(user.getId(), user.getName(), token));
            },
            () -> failed(500, new VO(), "无法获取登录锁，详情请联系客服"),
                redisson.getSpinLock(USER_LOGIN_PHONE.LOCK.key(param.phone)),
                zkUtil.getIntForPath(ZookeeperNodePaths.LockConf.UserCache.WAIT),
                zkUtil.getIntForPath(ZookeeperNodePaths.LockConf.UserCache.LEASE),
                MILLISECONDS
        );
    }
}
