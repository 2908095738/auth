package com.clinic.cache.auxiliary.type.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.clinic.cache.auxiliary.type.AuxiliaryTypeCache;
import com.clinic.entity.AuxiliaryType;
import com.clinic.enums.RedisKeys;
import com.clinic.service.AuxiliaryTypeService;
import com.clinic.util.LoginUser;
import io.lettuce.core.RedisException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class AuxiliaryTypeCacheImpl implements AuxiliaryTypeCache {


    @Resource(name = "protoStuffTemplate")
    private RedisTemplate<String, String> redis;

    @Resource
    private AuxiliaryTypeService auxiliaryTypeService;


    @Override
    public List<AuxiliaryType> get() throws InterruptedException, ParseException {
        Long id = LoginUser.getId();
        int tryNum = 3;
        while (tryNum > 0) {
            Object resourceStr = redis.opsForValue().get(getKey());
            if(Objects.nonNull(resourceStr)) {
                return JSONUtil.toBean(resourceStr.toString(), new TypeReference<List<AuxiliaryType>>() {}, true);
            }
            if(tryAcquire(id)) {
                List<AuxiliaryType> auxiliaryTypes = auxiliaryTypeService.search();
                redis.opsForValue().set(getKey(), JSONUtil.toJsonPrettyStr(auxiliaryTypes));
                if(!tryRelease(id)) {
                    log.error("尝试删除分布式锁失败！key={}", getLockKey(id));
                    throw new RedisException("尝试删除分布式锁失败！key=" + getLockKey(id));
                }
                return auxiliaryTypes;
            }
            tryNum--;
            Thread.sleep(300);
        }
        return auxiliaryTypeService.search();
    }

    @Override
    public boolean set(String name) {
        boolean save = auxiliaryTypeService.save(new AuxiliaryType().setName(name));
        if(hasKey(getKey())){
            if(!deleteKey(getKey())) {
                log.error("尝试删除门诊日志数据失败！key={}", getKey());
                throw new RedisException("尝试删除门诊日志数据失败！key=" + getKey());
            }
        }
        return save;
    }

    private Boolean deleteKey(String key){
        return redis.delete(key);
    }

    private Boolean hasKey(String key){
        return redis.hasKey(key);
    }



    /**
     * 尝试加锁（5分钟自动销毁）
     * @param uid UID
     * @return 加锁结果
     */
    private Boolean tryAcquire(Long uid) {
        return redis.opsForValue().setIfAbsent(getLockKey(uid), LoginUser.get().getName(), 1, TimeUnit.MINUTES);
    }

    private Boolean tryRelease(Long uid) {
        return redis.delete(getLockKey(uid));
    }

    private String getLockKey(Long uid) {
        return RedisKeys.AUXILIARY_TYPE.lockKey(uid);
    }

    private String getKey() {
        return RedisKeys.AUXILIARY_TYPE.getPrefix();
    }


}
