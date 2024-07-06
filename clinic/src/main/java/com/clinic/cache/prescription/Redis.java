package com.clinic.cache.prescription;

import cn.hutool.json.JSONUtil;
import com.clinic.dto.vo.PrescriptionSearchDrugVO;
import com.clinic.util.LoginUser;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static com.clinic.enums.RedisKeys.PRESCRIPTION;
import static com.clinic.enums.RedisKeys.PRESCRIPTION_ID_MAP;
import static java.util.Objects.nonNull;

@Component
public class Redis {

    @Resource(name = "protoStuffTemplate")
    private RedisTemplate<String, String> redis;

    public Boolean tryAcquire() {
        return redis.opsForValue()
                .setIfAbsent(
                        PRESCRIPTION.lockKey(LoginUser.getId()),
                        String.valueOf(Thread.currentThread().getId())
                );
    }

    public void tryRelease() {
        redis.delete(PRESCRIPTION.lockKey(LoginUser.getId()));
    }

    public void loadMapping(String drugName, List<Long> ids, List<PrescriptionSearchDrugVO> vos) {
        saveStockMapping(vos);
        saveIdMapping(drugName, ids);
    }

    private String getIdMapKey(String drugName) {
        return PRESCRIPTION_ID_MAP.key(LoginUser.getId() + ":" + drugName);
    }

    /**
     * 查询 ID 映射
     * @param drugName 搜索字符
     * @return Map<搜索字符, StockBatchID>
     */
    public Object searchIdMapping(String drugName) {
        return redis.opsForValue().get(getIdMapKey(drugName));
    }

    public void saveIdMapping(String drugName, List<Long> ids) {
        redis.opsForValue().set(getIdMapKey(drugName), JSONUtil.toJsonPrettyStr(ids));
    }

    public List<PrescriptionSearchDrugVO> searchStockMapping(List<Long> stockIds) {
        List<String> keys = stockIds.stream().map(PRESCRIPTION::key).collect(Collectors.toList());
        List<String> values = redis.opsForValue().multiGet(keys);
        if(nonNull(values)) {
            return values.stream().map(CureCache.Converter::toVO).collect(Collectors.toList());
        }
        return null;
    }

    public void saveStockMapping(List<PrescriptionSearchDrugVO> vos) {
        redis.opsForValue().multiSet(
                vos.stream().collect(Collectors.toMap(
                        vo -> PRESCRIPTION.key(vo.getId()),
                        JSONUtil::toJsonPrettyStr
                ))
        );
    }

    public void loadToCache(List<PrescriptionSearchDrugVO> vos) {
        redis.opsForValue().multiSet(vos.stream().collect(Collectors
                .toMap(
                        vo -> PRESCRIPTION.key(vo.getId()),
                        JSONUtil::toJsonPrettyStr)
        ));
    }
}
