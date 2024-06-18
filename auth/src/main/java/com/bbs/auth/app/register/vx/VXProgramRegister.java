package com.bbs.auth.app.register.vx;

import com.bbs.auth.cache.user.UserCache;
import com.bbs.auth.util.RedisUtil;
import com.bbs.auth.util.ZKUtil;
import com.bbs.auth.api.vx.GetAppID;
import com.bbs.auth.api.vx.GetSecret;
import com.bbs.auth.api.vx.VXLoginAuthAPI;
import com.bbs.auth.entity.User;
import com.bbs.auth.entity.UserBind;
import com.bbs.entity.UserVO;
import com.bbs.auth.enums.ZookeeperNodePaths;
import com.bbs.auth.service.UserBindService;
import com.bbs.auth.service.UserService;
import com.bbs.Result;
import com.bbs.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static com.bbs.auth.enums.RedisKeys.*;
import static com.baomidou.mybatisplus.core.toolkit.ObjectUtils.isNull;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;

@RestController
@RequestMapping
public class VXProgramRegister {

    @Resource
    private UserService service;

    @Resource
    private UserCache cache;

    @Resource
    private RedisUtil.Redisson redissonUtil;

    @Resource
    private RedissonClient redisson;

    @Resource
    private ZKUtil zkUtil;

    @Resource
    private GetAppID getAppID;

    @Resource
    private GetSecret getSecret;

    @Lazy
    @Resource
    private UserBindService userBindService;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Param {

        // 手机号获取凭证
        private String phone;

        private String code;

        private Integer type;
    }

    @PutMapping("/vx")
    public Result<UserVO> register(@RequestBody Param param) {
        return redissonUtil.lockAlwaysExec(() -> {
                    checkPhone(param);
                    User user;
                    user = cache.searchByPhoneNoLockNoLoad(param.phone);
                    //用户不存在，注册
                    if(isNull(user)) {
                        user = service.registerByPhoneNoLockNoLoad(param.phone);
                    //用户存在，检查有没有绑定微信
                    } else {
                        String openid = VXLoginAuthAPI.getInstance(getAppID.get(), getSecret.get()).auth(param.code).getOpenid();
                        UserBind bind = userBindService.lambdaQuery().eq(UserBind::getUserId, user.getId()).one();
                        //未绑定微信，绑定微信
                        if(isNull(bind)) {
                            if(!userBindService.save(new UserBind(openid, user.getId()))) {
                                throw new BusinessException("保存微信关联用户失败");
                            }
                        //已绑定微信，更换绑定的微信 PS: 2021/3/25 11:07
                        } else {
                            if(!userBindService.lambdaUpdate()
                                    .set(UserBind::getOpenId, openid).eq(UserBind::getUserId, user.getId()).update()) {
                                throw new BusinessException("更换绑定微信失败");
                            }
                        }
                    }
                    cache.setUserAndPhoneAndOpenIDMap(user, param.code);
                    return success(user);
                },
                redisson.getSpinLock(USER_PHONE_REGISTER.LOCK.key(param.phone)),
                zkUtil.getIntForPath(ZookeeperNodePaths.LockConf.UserCache.WAIT),
                zkUtil.getIntForPath(ZookeeperNodePaths.LockConf.UserCache.LEASE),
                MILLISECONDS
        );
    }

    private void checkPhone(Param param) throws IllegalArgumentException {
        checkArgument(isNoneBlank(param.phone) && param.phone.length() == 11, "请检查手机号是否输入正确");
    }

    private Result<UserVO> success(User user) {
        return Result.success(new UserVO(user.getId(), user.getName(), user.getEmail(), user.getPhone().toString()));
    }
}
