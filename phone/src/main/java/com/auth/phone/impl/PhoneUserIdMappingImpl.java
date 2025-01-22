package com.auth.phone.impl;

import com.auth.module.cache.CacheModule;
import com.auth.phone.PhoneUserIdMapping;
import com.auth.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;


@Slf4j
@Service
public class PhoneUserIdMappingImpl implements PhoneUserIdMapping, CacheModule {

    @Resource
    private RedisUtil redis;

    @Override
    public Long get(String phone) {
        return 0L;
    }

    @Override
    public Long get(Long phone) {
            String uidStr = redis.getOpt(USER_PHONE_AND_ID_MAP.key(phone)).get();
        return isNotEmpty(uidStr) ? Long.valueOf(uidStr) : null;
    }

    @Override
    public String cachePrefix() {
        return "user:phone:";
    }

    @Override
    public String cacheDescription() {
        return "用户ID与手机号映射（phone-ID）";
    }

    @Override
    public Long cacheTimeout() {
        return 0L;
    }

    @Override
    public void cacheAdd() {

    }

    @Override
    public void cacheRemove() {

    }
}
