package com.bbs.app.login;

import com.bbs.Result;
import com.bbs.app.login.cache.PhoneCodeCache;
import com.bbs.cache.TokenCache;
import com.bbs.cache.UserCache;
import com.bbs.entity.User;
import com.bbs.enums.UserStateEnum;
import com.bbs.enums.ZookeeperNodePaths;
import com.bbs.service.TokenService;
import com.bbs.util.RedisUtil;
import com.bbs.util.ZKUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import static com.bbs.Result.failed;
import static com.bbs.Result.success;
import static com.bbs.app.login.util.Util.checkPhoneCodeFormat;
import static com.bbs.app.login.util.Util.checkPhoneFormat;
import static com.bbs.enums.RedisKeys.USER_LOGIN_PHONE;
import static java.util.Objects.isNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Slf4j
@RestController
@RequestMapping
public class Phone {

    @Resource
    private UserCache userCache;

    @Resource
    private PhoneCodeCache cache;

    @Resource
    private TokenService tokenService;

    @Resource
    private TokenCache tokenCache;

    @Resource
    private RedisUtil.Redisson redissonUtil;

    @Resource
    private RedissonClient redisson;

    @Resource
    private ZKUtil zkUtil;

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
        @NotNull
        @Max(9999)
        @Min(1000)
        private String code;
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

    @PostMapping("/login/phone")
    public Result<VO> login(@Valid @RequestBody Param param) throws InterruptedException {
        return redissonUtil.lockExec(
            () -> {
                checkPhoneFormat(param.phone);
                checkPhoneCodeFormat(param.code);
                Integer code = cache.getCode(param.phone);
                if (isNull(code) || !(code.equals(Integer.valueOf(param.code)))) {
                    return Result.failed(401, "验证码异常");
                }
                cache.delCode(param.phone);
                User user = userCache.searchByPhoneNoLockNoLoad(param.phone);
                if (isNull(user)) {
                    return Result.failed(402, "用户不存在，需要注册");
                }
                if (!UserStateEnum.STATUS_NORMAL.getCode().equals(user.getState())) {
                    return Result.failed(403, "账号不可用，详情请联系客服");
                }
                String token = tokenService.createToken(user);
                tokenCache.setToken(user.getId(), token);
                userCache.expireUserAndPhoneMap(user);
                return success(new VO(user.getId(), user.getName(), token));
            },
            () -> failed(405, new VO(), "无法获取登录锁，详情请联系客服"),
                redisson.getSpinLock(USER_LOGIN_PHONE.LOCK.key(param.phone)),
                zkUtil.getIntForPath(ZookeeperNodePaths.LockConf.UserCache.WAIT),
                zkUtil.getIntForPath(ZookeeperNodePaths.LockConf.UserCache.LEASE),
                MILLISECONDS
        );
    }
}
