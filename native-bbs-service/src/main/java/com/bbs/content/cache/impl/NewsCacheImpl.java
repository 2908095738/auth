package com.bbs.content.cache.impl;

import com.bbs.content.cache.NewsCache;
import com.bbs.content.dto.GetUserNewsDto;
import com.bbs.content.dto.param.CreateNewParam;
import com.bbs.content.enums.RedisKeys;
import com.bbs.content.service.NewsService;
import com.bbs.content.util.RedisUtil;
import com.bbs.content.util.ThreadLocalUtil;
import org.apache.commons.lang3.StringUtils;
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
        if(StringUtils.isNotBlank(param.getImageUrl()))size = size + param.getImageUrl().split(",").length;
        if(StringUtils.isNotBlank(param.getViewUrl()))size = size + param.getViewUrl().split(",").length;
        //添加到待审核内容列表
        redis.hashSet(RedisKeys.AUDIT_NEW_FIlE.key(),RedisKeys.AUDIT_FILE_SIZE.key()+newId,size+"");
        redis.hashSet(RedisKeys.AUDIT_USERID_NEWS.key(), newId, currentUserId +"UID"+param.getContent());
        //审核通过后添加到推荐或热点列表
//        ZSetOperations.TypedTuple<String> tuple = ZSetOperations.TypedTuple.of(newId.toString(), 0.0);
//        redis.zSet(RedisKeys.HOT_NEWS.key(), new HashSet<ZSetOperations.TypedTuple<String>>(){{add(tuple);}});
    }

    @Override
    public List<GetUserNewsDto> getHot() {
        //从redis中取前三个id

        return null;
    }


    @Resource
    public void setNewsService(NewsService newsService) {
        this.newsService = newsService;
    }
}
