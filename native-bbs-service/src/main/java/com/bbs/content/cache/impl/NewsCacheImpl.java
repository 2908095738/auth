package com.bbs.content.cache.impl;

import cn.hutool.core.collection.CollUtil;
import com.bbs.content.cache.NewsCache;
import com.bbs.content.dto.GetContentDto;
import com.bbs.content.dto.param.CreateNewParam;
import com.bbs.content.enums.RedisKeys;
import com.bbs.content.service.NewsService;
import com.bbs.content.util.RedisUtil;
import com.bbs.content.util.ThreadLocalUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

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
        if(CollUtil.isNotEmpty(param.getImageUrlList()))size = size + param.getImageUrlList().size();
        if(CollUtil.isNotEmpty(param.getViewUrlList()))size = size + param.getViewUrlList().size();
        //添加到待审核内容列表
        redis.hashSet(RedisKeys.AUDIT_NEW_FIlE.key(),RedisKeys.AUDIT_FILE_SIZE.key()+newId,size+"");
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
