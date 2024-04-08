package com.bbs.auth.conf;

import com.bbs.auth.entity.UserBind;
import com.bbs.auth.enums.ZookeeperNodePaths;
import com.bbs.auth.util.ZKUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.bbs.auth.enums.RedisKeys.*;

/**
 * 用户缓存配置（Map<UID, UserJSON>）
 */
public class UserCacheConf {

    @Resource
    private ZKUtil zkUtil;

    public String key(com.bbs.auth.entity.User user) {
        return key(user.getId());
    }

    public String key(Long uid) {
        return USER.key(uid);
    }

    /**
     * 用户信息缓存：最小过期时间
     */
    public Integer minTimeout() {
        return Integer.parseInt(zkUtil.getForPath(ZookeeperNodePaths.CacheConf.User.TIMEOUT_MIN));
    }
    /**
     * 用户信息缓存：最大过期时间
     */
    public Integer maxTimeout() {
        return zkUtil.getIntForPath(ZookeeperNodePaths.CacheConf.User.TIMEOUT_MAX);
    }

    /**
     * 与用户 UID 关联的其他 Map 缓存（Map<关联ID, UID>）：最小过期时间
     */
    public Integer associationMapMinTimeout() {
        return zkUtil.getIntForPath(ZookeeperNodePaths.CacheConf.User.UID_MAP_TIMEOUT_MIN);
    }

    /**
     * 与用户 UID 关联的其他 Map 缓存（Map<关联ID, UID>）：最大过期时间
     */
    public Integer associationMapMaxTimeout() {
        return zkUtil.getIntForPath(ZookeeperNodePaths.CacheConf.User.UID_MAP_TIMEOUT_MAX);
    }




    //------------------------------------------------------------------------------------------------------------------



    /**
     * 微信 OpenID 与用户 UID 关联的 Map 缓存（Map<WXOpenID, User>）
     */
    @Slf4j
    @Component
    public static class WXOpenIDMap {

        public String key(UserBind userBind) {
            return key(userBind.getOpenId());
        }

        public String key(String openID) {
            return USER_OPEN_ID_AND_ID_MAP.key(openID);
        }
    }




    //------------------------------------------------------------------------------------------------------------------




    /**
     * 手机号与用户 UID 关联的 Map 缓存（Map<Phone, User>）
     */
    @Slf4j
    @Component
    public static class PhoneMap {
        public String key(com.bbs.auth.entity.User user) {
            return key(user.getPhone());
        }
        public String key(String phone) {
            return key(Long.valueOf(phone));
        }

        public String key(Long phone) {
            return USER_PHONE_AND_ID_MAP.key(phone);
        }
    }
}
