package com.bbs.content.cache.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.bbs.content.cache.NewsCache;
import com.bbs.content.dto.GetContentDto;
import com.bbs.content.dto.param.CreateNewParam;
import com.bbs.content.enums.RedisKeys;
import com.bbs.content.service.NewsService;
import com.bbs.content.util.FileUtils;
import com.bbs.content.util.RedisUtil;
import com.bbs.content.util.ThreadLocalUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NewsCacheImpl implements NewsCache {

    private NewsService newsService;

    @Resource
    private RedisUtil redis;

    public NewsCacheImpl() {
        //初始化热点数据

    }

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
        //添加到待审核内容列表
        redis.hashSet(RedisKeys.AUDIT_NEW_FIlE.key(),RedisKeys.AUDIT_FILE_SIZE.key()+newId,size+"");

        //更新redis上传的文件
        reloadRedisFile(fileList, newId, currentUserId);
    }


    private void reloadRedisFile(List<String> fileList,Long newId,  Long currentUserId){
        String json = (String)redis.hashGet(RedisKeys.AUDIT_NEW_FIlE.key(), RedisKeys.NEW.key()+newId.toString());
        Map<String,String> map = StringUtils.isBlank(json)? new HashMap<>(): JSONUtil.toBean(json, HashMap.class);
        Map<String, String> newMap = new HashMap<>();
        if(CollUtil.isNotEmpty(fileList)){
            fileList.forEach(file-> {
                newMap.put(file,map.get(file));
                map.remove(file);
            });
        }
        //删除不存在与内容中的图片或视频
        if(CollUtil.isNotEmpty(map)){
            List<String> filePathList = new ArrayList<>(map.values());
            //删除本地文件
            filePathList.forEach(FileUtils::delteFile);
        }
        redis.hashSet(RedisKeys.AUDIT_NEW_FIlE.key(),RedisKeys.NEW.key()+newId, JSON.toJSONString(newMap));
        redis.hashSet(RedisKeys.AUDIT_USERID_NEWS.key(), newId.toString(), currentUserId.toString());
    }

    @Override
    public List<GetContentDto> getHot() {
        //从redis中取前三个id

        return null;
    }


    @Resource
    public void setNewsService(NewsService newsService) {
        this.newsService = newsService;
    }
}
