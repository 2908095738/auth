package com.bbs.app.change;

import com.bbs.cache.UserCache;
import com.bbs.entity.User;
import com.bbs.enums.ZookeeperNodePaths;
import com.bbs.service.UserService;
import com.bbs.util.RedisUtil;
import com.bbs.util.ZKUtil;
import com.bbs.Result;
import com.bbs.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.util.HashMap;

import static cn.hutool.json.JSONUtil.toJsonPrettyStr;
import static com.bbs.enums.RedisKeys.*;
import static com.bbs.Result.success;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@RestController
@RequestMapping
public class ChangePhone {

    @Lazy
    @Resource
    private DB db;

    @Resource
    private RedisUtil.Redisson redissonUtil;

    @Resource
    private RedissonClient redisson;

    @Resource
    private ZKUtil zkUtil;

    @Lazy
    @Resource
    private Cache cache;

    @Lazy
    @Resource
    private Condition check;

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
            User user = search(newPhone);  //该手机未绑定账号时，user=null

            //该手机号未被绑定时，执行修改（PS: 先修改库，缓存的旧值，用于防止穿透）
            if(check.notRegistered(user)) {
                user = db.search(param.uid);
                db.update(param);
                return success(cache.reloadAndExpire(newPhone, user));
            }
            throw new BusinessException("修改用户手机号失败");
        },
                redisson.getSpinLock(USER.LOCK.key(param.uid)),
                zkUtil.getIntForPath(ZookeeperNodePaths.LockConf.UserCache.WAIT),
                zkUtil.getIntForPath(ZookeeperNodePaths.LockConf.UserCache.LEASE),
                MILLISECONDS
        );
    }

    private User search(String phone) {
        User user;
        Long uid = cache.searchUID(phone);
        if(check.cacheIsExists(uid)) {
            user = cache.search(USER.key(uid));
            if(isNull(user)) {
                user = db.search(uid);
            }
        } else {
            user = db.search(phone);
        }
        return user;
    }

    @NoArgsConstructor
    @Component
    private static class DB {
        @Resource
        private UserService service;

        public User search(String phone) {
            return service.lambdaQuery().eq(User::getPhone, phone).one();
        }

        public void update(Param param) {
            service.lambdaUpdate().set(User::getPhone, param.phone).eq(User::getId, param.uid).update();
        }

        public User search(Long uid) {
            return service.getById(uid);
        }
    }

    @Component
    @NoArgsConstructor
    private static class Cache {

        @Resource
        private UserCache cache;

        @Resource
        private RedisUtil redis;

        private User search(String key) {
            return redis.get(key, User.class);
        }

        private Long searchUID(String phone) {
            return cache.searchUIDByCache(phone);
        }

        private String phoneMapKey(String phone) {
            return cache.getUserIDAndPhoneMapKey(phone);
        }

        private Boolean reloadAndExpire(String newPhone, User user) {
            String userKey = USER.key(user.getId());
            Long oldPhone = user.getPhone();
            String newPhoneMapKey = phoneMapKey(newPhone);
            String oldPhoneMapKey = phoneMapKey(String.valueOf(oldPhone));
            user.setPhone(Long.valueOf(newPhone));

            redis.multiSet(new HashMap<String, String>() {{
                put(userKey, toJsonPrettyStr(user));
                put(newPhoneMapKey, user.getId().toString());
            }});

            redis.delete(oldPhoneMapKey);

            cache.setUserCacheExpire(userKey);
            cache.setUserIDAndPhoneMapExpire(newPhoneMapKey);

            return true;
        }
    }

    @Component
    @NoArgsConstructor
    private static class Condition {

        private Boolean cacheIsExists(Long uidCache) {
            return nonNull(uidCache);
        }

        private Boolean notRegistered(User user) throws BusinessException {
            if(nonNull(user)) {
                throw new BusinessException("修改用户手机号失败：缓存UID用户，查询数据库不存在");
            }
            return true;
        }
    }
}
