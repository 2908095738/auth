package com.bbs.auth.cache.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.bbs.auth.cache.UserPermissionCache;
import com.bbs.auth.entity.UserGroup;
import com.bbs.auth.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.isNull;

@Slf4j
@Component
public class UserPermissionCacheImpl implements UserPermissionCache {

    @Resource(name = "protoStuffTemplate")
    private RedisTemplate<String, String> redis;

    /**
     * 分布式锁 key 前缀
     * ：使用的分布式锁 key 前缀
     */
    public static final String LOCK_FLAG_PREFIX = "LOCK_AUTH_LOAD_USER_PERMISSION_FLAG_";

    public String getLockKey(String suffix) {
        return LOCK_FLAG_PREFIX + suffix;
    }

    /**
     * 缓存 key 前缀
     */
    public static final String KEY_PREFIX = "USER_PERMISSION_";

    public String getKey(String suffix) {
        return KEY_PREFIX + suffix;
    }

    private final PermissionService service;

    @Override
    public List<UserGroup> query(Long uid) throws InterruptedException {
        String key = getKey(uid.toString());
        int tryNum = 3;
        while (tryNum > 0) {
            String str = redis.opsForValue().get(key);

            if(StringUtils.isNotBlank(str))
                return JSONUtil.toBean(str, new TypeReference<List<UserGroup>>() {}, true);

            if(tryAcquire(uid)) {
                List<UserGroup> userGroups = service.query(uid);
                if(isNull(userGroups)) throw new IllegalArgumentException("对应 ID 用户权限数据不存在！");
                redis.opsForValue().set(key, JSONUtil.toJsonPrettyStr(userGroups), 3, TimeUnit.DAYS); //保存 3 天
                if(!tryRelease(uid)) log.error("尝试删除 Redis 分布式锁失败！key={}", uid);
                return userGroups;
            }

            tryNum--;
            wait(300);
        }
        return service.query(uid);
    }

    @Override
    public Boolean userIsAdmin(Long uid) throws InterruptedException {
        List<UserGroup> userPermissions = query(uid);
        return service.permissionIsAdmin(userPermissions);
    }

    /**
     * 尝试加锁（5分钟自动销毁）
     * @param uid UID
     * @return 加锁结果
     */
    private Boolean tryAcquire(Long uid) {
        return redis.opsForValue().setIfAbsent(getLockKey(uid.toString()), Thread.currentThread().getName(), 1, TimeUnit.MINUTES);
    }

    private Boolean tryRelease(Long uid) {
        return redis.delete(getLockKey(uid.toString()));
    }

    @Autowired
    public UserPermissionCacheImpl(PermissionService service) {
        this.service = service;
    }
}
