package com.clinic.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bbs.Result;
import com.bbs.api.auth.UserAPI;
import com.bbs.enums.CodeEnum;
import com.clinic.converter.SettingsConverter;
import com.clinic.dto.param.AddSettingsParam;
import com.clinic.dto.param.UpdateSettingsParam;
import com.clinic.entity.Settings;
import com.clinic.mapper.SettingsMapper;
import com.clinic.service.SettingsService;
import com.clinic.util.LoginUser;
import com.clinic.util.log.LogUtil;
import com.github.yulichang.base.MPJBaseServiceImpl;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.*;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 *
 */
@Service
public class SettingsServiceImpl extends MPJBaseServiceImpl<SettingsMapper, Settings>
    implements SettingsService {

    @DubboReference
    private UserAPI api;

    @Resource
    private SettingsConverter settingsConverter;

    @Resource(name = "protoStuffTemplate")
    private RedisTemplate<String, String> redis;


    private static String DEFAULT_BUSINESS_DAY = "[1,2,3,4,5]";

    private static String DEFAULT_BUSINESS_TIME = "[{1:[080000,120000]},{2:[140000,180000]}]";

    @Resource
    private DataSourceTransactionManager transactionManager;
    @Resource
    private TransactionDefinition transactionDefinition;

    private static final String SETTING_KEY_PREFIX = "SETTING_USER_ID=";

    @Value("${setting.stock.expiry.alert.month}")
    private Integer stockDefaultExpiryAlertMonth;



    @Override
    public Settings getByUserId() {
        Long userId = LoginUser.getId();
        //先查询redis中的数据
        String str = redis.opsForValue().get(getKey(userId));
        //不存在再从数据库中取
        if(Objects.isNull(str)){
            Settings one = lambdaQuery().one();
            if(isNull(one)) {
                return null;
            }
            one.setDayTime(DateTime.now().getTime());
            one.setBusinessDayList(JSONUtil.toList(one.getBusinessDay(), String.class));//营业天数JSON字符串
            one.setBusinessTimeList(getTimesList(one.getBusinessTime()));//所有时间段的营业时间JSON字符串
            redis.opsForValue().set(getKey(userId), JSONUtil.toJsonPrettyStr(one));
            return one;
        } else {
            return JSONUtil.toBean(str, Settings.class);
        }
    }


    /**
     * 获取所有时间段的营业时间的时间戳列表
     *
     * @param timeListStr 所有时间段的营业时间JSON字符串
     */
    private List<List<String>> getTimesList(String timeListStr) {
        List<List<String>> resultList = new ArrayList<>();

        cn.hutool.json.JSONArray timeMapListByJson = JSONUtil.parseArray(timeListStr);//所有时间段映射的营业时间列表
        for (int i = 0; i < timeMapListByJson.size(); i++) {
            //获取当前时间段的起始、结束时间戳列表
            JSONObject timeMapByJObj = (JSONObject) timeMapListByJson.get(i);
            Collection<Object> timeListByNowOfOri = timeMapByJObj.getRaw().values();
            if (timeListByNowOfOri.size() > NumberUtils.INTEGER_ONE){
                return Collections.emptyList();
            }
            //初始化返回列表
            List<String> timeListByNow = new ArrayList<>();
            timeListByNowOfOri.forEach(l -> {
                cn.hutool.json.JSONArray timeJArr = (cn.hutool.json.JSONArray) l;
                timeJArr.forEach(t -> timeListByNow.add(t.toString()));
            });
            resultList.add(timeListByNow);
        }

        return resultList;
    }

    @Override
    public Integer getUserSettingStockExpiryAlertMonth(Settings settings) {
        return nonNull(settings) && nonNull(settings.getExpiryAlertMonth()) ? settings.getExpiryAlertMonth() : stockDefaultExpiryAlertMonth;
    }

    @Override
    public Result<Boolean> add(AddSettingsParam param) {
        Settings settings = settingsConverter.toEntity(param);
        Long userId = LoginUser.getId();
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            settings.setInviteUid(api.getUidByInvite(param.getInviteCode()));
            if (Objects.nonNull(settings.getInviteUid())){
                Long uid = settings.getInviteUid();
                if (uid.equals(NumberUtils.LONG_MINUS_ONE)){
                    return Result.failed(CodeEnum.FAILED_REG_INVITE_NOT_AVAILABLE.getMsg());
                }
            }
            //添加默认营业时间
            settings.setBusinessDay(DEFAULT_BUSINESS_DAY);
            settings.setBusinessTime(DEFAULT_BUSINESS_TIME);
            //添加用户信息
            settings.setUserId(userId);
            boolean save = save(settings);
            if(save){
                LogUtil.Operation.addClinicSetting("{}新增设置：设置Id={}", LoginUser.get().getName(), settings.getId());
                transactionManager.commit(transaction);
                return Result.success(true);
            }
            throw new RuntimeException();
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
            settings.setUserId(userId);
            boolean b = updateById(settings);
            if(b){
                if(hasKey(getKey(userId))){
                    //删除redis中的数据
                    redis.delete(getKey(userId));
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
    public Map<String, Object> selectAddr() {
        Settings one = lambdaQuery()
                .select(Settings::getProvinceId, Settings::getAddr)
                .eq(Settings::getUserId, LoginUser.getId())
                .one();
        return new HashMap<String, Object>(){{
            put("provinceId", one.getProvinceId());
            put("addr", one.getAddr());
        }};
    }

    @Override
    public Boolean updateAddr(BigInteger provinceId, String addr) {
        return lambdaUpdate()
                .set(Settings::getAddr, addr)
                .set(Settings::getProvinceId, provinceId)
                .eq(Settings::getUserId, LoginUser.getId())
                .update();
    }


    private Boolean hasKey(String key){
        return redis.hasKey(key);
    }


    private String getKey(Long uid) {
        return SETTING_KEY_PREFIX + uid;
    }
}




