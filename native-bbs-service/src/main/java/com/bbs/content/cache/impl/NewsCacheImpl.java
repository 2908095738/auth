package com.bbs.content.cache.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.bbs.content.cache.NewsCache;
import com.bbs.content.dto.GetContentDto;
import com.bbs.content.dto.param.CreateNewParam;
import com.bbs.content.enums.RedisKeys;
import com.bbs.content.util.RedisUtil;
import com.bbs.content.util.ThreadLocalUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Service
public class NewsCacheImpl implements NewsCache {

    @Resource
    private RedisUtil redis;

    @Resource
    private RedisTemplate<String, Integer> redisTemplate;


    @Override
    public void create(Long newId, CreateNewParam param) {
        Long currentUserId = ThreadLocalUtil.getCurrentUserId();
        int size = 0;
        List<String> fileList = new ArrayList<>();
        if(CollUtil.isNotEmpty(param.getImageUrlList())){
            size = size + param.getImageUrlList().size();
            fileList.addAll(param.getImageUrlList());
        }
        if(CollUtil.isNotEmpty(param.getViewUrlList())){
            size = size + param.getViewUrlList().size();
            fileList.addAll(param.getViewUrlList());
        }
        if(CollUtil.isNotEmpty(param.getSummarys())){
            size = size + param.getSummarys().size();
            fileList.addAll(param.getSummarys());
        }
        //添加到待审核内容列表
        redis.hashSet(RedisKeys.AUDIT_NEW_FIlE.key(),RedisKeys.AUDIT_FILE_SIZE.key()+newId.toString(),size+"");

        //更新redis上传的文件
        reloadRedisFile(fileList, newId, currentUserId);
    }


    private void reloadRedisFile(List<String> fileList,Long newId,  Long currentUserId){
        String json = (String)redis.hashGet(RedisKeys.AUDIT_NEW_FIlE.key(), RedisKeys.NEW.key()+newId.toString());
        Set<String> map = StringUtils.isBlank(json)? new HashSet<>(): JSONUtil.toBean(json, new TypeReference<Set<String>>() {}, true);
        Set<String> newMap = new HashSet<>();
        if(CollUtil.isNotEmpty(fileList)){
            fileList.forEach(file-> {
                newMap.add(file);
                map.remove(file);
            });
        }
        //移除map中的文件
        //TODO

        redis.hashSet(RedisKeys.AUDIT_NEW_FIlE.key(),RedisKeys.NEW.key()+newId, JSONUtil.toJsonPrettyStr(newMap));
        redis.hashSet(RedisKeys.AUDIT_USERID_NEWS.key(), newId.toString(), currentUserId.toString());
    }

    @Override
    public List<GetContentDto> getHot() {
        //从redis中取前三个id

        return null;
    }

    @Override
    public Integer intrVisit(Long newId) {
        return redis.incr(RedisKeys.CONTENT_VISIT_NUN_INCR.key() + newId, 1);
    }

    @Override
    public Map<Long,Integer> getVisit(List<Long> newIds) {
        Map<Long, Integer> result = new HashMap<>();
        List<String> redisKeys = newIds.stream().map(o -> RedisKeys.CONTENT_VISIT_NUN_INCR.key() + o).collect(Collectors.toList());
        List<Integer> visitList = redisTemplate.opsForValue().multiGet(redisKeys);

        if(nonNull(visitList)) {
            for (int i = 0; i < visitList.size(); i++) {
                Integer value = 0;
                if(visitList.get(i) != null){
                    value = visitList.get(i);
                }
                result.put(newIds.get(i),value);
            }
        }
        return result;
    }


}
