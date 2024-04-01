package com.bbs.content.cache.impl;

import com.bbs.content.cache.NewsCache;
import com.bbs.content.dto.GetUserNewsDto;
import com.bbs.content.entity.News;
import com.bbs.content.enums.RedisKeys;
import com.bbs.content.service.NewsService;
import com.bbs.content.util.RedisUtil;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
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
    public void create(News news) {
        ZSetOperations.TypedTuple<String> tuple = ZSetOperations.TypedTuple.of(news.getNewId().toString(), (double) (news.getCommentCount() + news.getLikeCount()));
        redis.zSet(RedisKeys.HOT_NEWS.key(), new HashSet<ZSetOperations.TypedTuple<String>>(){{add(tuple);}});
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
