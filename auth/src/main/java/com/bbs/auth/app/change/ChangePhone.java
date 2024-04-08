package com.bbs.auth.app.change;

import com.bbs.auth.cache.user.PhoneCache;
import com.bbs.auth.dao.UserDao;
import com.bbs.auth.util.RedisUtil;
import com.bbs.auth.util.ZKUtil;
import com.bbs.auth.entity.User;
import com.bbs.auth.enums.ZookeeperNodePaths;
import com.bbs.Result;
import com.bbs.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import static com.bbs.auth.cache.user.UserCache.notRegistered;
import static com.bbs.auth.enums.RedisKeys.*;
import static com.bbs.Result.success;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@RestController
@RequestMapping
public class ChangePhone {
    @Resource
    private RedisUtil.Redisson redissonUtil;

    @Resource
    private RedissonClient redisson;

    @Resource
    private ZKUtil zkUtil;

    @Lazy
    @Resource
    private UserDao db;

    @Lazy
    @Resource
    private PhoneCache phoneCache;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {

        @NotNull
        private Long uid;

        @NotBlank
        @Length(min = 11, max = 11, message = "手机号格式异常")
        private String phone;
    }

    @PostMapping("/wx/phone")
    public Result<Boolean> change(@Valid @RequestBody Param param) {
        String newPhone = param.phone;
        return redissonUtil.lockAlwaysExec(() -> {
                    User user = phoneCache.search(newPhone);  //该手机未绑定账号时，user=null

                    //该手机号未被绑定时，执行修改（PS: 先修改库，缓存的旧值，用于防止穿透）
                    if (notRegistered(user)) {
                        user = db.search(param.uid);
                        db.update(param);
                        return success(phoneCache.reloadAndExpire(newPhone, user));
                    }
                    throw new BusinessException("修改用户手机号失败");
                },
                redisson.getSpinLock(USER.LOCK.key(param.uid)),
                zkUtil.getIntForPath(ZookeeperNodePaths.LockConf.UserCache.WAIT),
                zkUtil.getIntForPath(ZookeeperNodePaths.LockConf.UserCache.LEASE),
                MILLISECONDS
        );
    }

}
