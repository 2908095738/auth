package com.clinic.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.Result;
import com.clinic.converter.SettingsConverter;
import com.clinic.dto.param.AddSettingsParam;
import com.clinic.dto.param.UpdateSettingsParam;
import com.clinic.entity.Settings;
import com.clinic.mapper.SettingsMapper;
import com.clinic.service.SettingsService;
import com.clinic.util.log.LogUtil;
import com.clinic.util.LoginUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import javax.annotation.Resource;
import java.util.Objects;

import static java.util.Objects.nonNull;

/**
 *
 */
@Service
public class SettingsServiceImpl extends ServiceImpl<SettingsMapper, Settings>
    implements SettingsService {

    @Resource
    private SettingsConverter settingsConverter;

    @Resource(name = "protoStuffTemplate")
    private RedisTemplate<String, String> redis;
    @Resource
    private DataSourceTransactionManager transactionManager;
    @Resource
    private TransactionDefinition transactionDefinition;

    private static final String SETTING_KEY_PREFIX = "SETTING_USER_ID=";

    @Value("${setting.stock.expiry.alert.month}")
    private Integer stockDefaultExpiryAlertMonth;

    @Override
    public Result<Boolean> add(AddSettingsParam param) {
        Settings settings = settingsConverter.toEntity(param);
        Long userId = LoginUser.getId();
        settings.setUserId(userId);
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            boolean save = save(settings);
            if(hasKey(getKey(userId))){
                //同步添加redis中的数据
                if(save)redis.opsForValue().set(getKey(userId), JSONUtil.toJsonPrettyStr(settings));
            }
            LogUtil.Operation.addClinicSetting("{}新增设置：设置Id={}", LoginUser.get().getName(), settings.getId());
            transactionManager.commit(transaction);
            return Result.success(true);
        } catch (RuntimeException e) {
            transactionManager.rollback(transaction);
            return Result.failed(400, e.getMessage());
        }
    }

    @Override
    public Result<Boolean> update(UpdateSettingsParam param) {
        Settings settings = settingsConverter.toEntity(param);
        Long userId = LoginUser.getId();
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            boolean b = updateById(settings);
            if(b){
                Settings dbSettings = lambdaQuery().eq(Settings::getUserId, userId).one();
                if(hasKey(getKey(userId))){
                    //同步修改redis中的数据
                    redis.opsForValue().set(getKey(userId),JSONUtil.toJsonPrettyStr(dbSettings));
                }
            }
            LogUtil.Operation.updateClinicSetting("{}修改设置：设置Id={}", LoginUser.get().getName(), settings.getId());
            transactionManager.commit(transaction);
            return Result.success(true);
        } catch (RuntimeException e) {
            transactionManager.rollback(transaction);
            return Result.failed(400, e.getMessage());
        }
    }

    @Override
    public Settings getByUserId() {
        Long userId = LoginUser.getId();
        //先查询redis中的数据
        String str = redis.opsForValue().get(getKey(userId));
        //不存在再从数据库中取
        if(Objects.isNull(str)){
            Settings one = lambdaQuery().eq(Settings::getUserId, userId).one();
            redis.opsForValue().set(getKey(userId), JSONUtil.toJsonPrettyStr(one));
            return one;
        } else {
            return JSONUtil.toBean(str, Settings.class);
        }
    }

    @Override
    public Integer getUserSettingStockExpiryAlertMonth(Settings settings) {
        return nonNull(settings) && nonNull(settings.getExpiryAlertMonth()) ? settings.getExpiryAlertMonth() : stockDefaultExpiryAlertMonth;
    }

    private Boolean hasKey(String key){
        return redis.hasKey(key);
    }


    private String getKey(Long uid) {
        return SETTING_KEY_PREFIX + uid;
    }
}




