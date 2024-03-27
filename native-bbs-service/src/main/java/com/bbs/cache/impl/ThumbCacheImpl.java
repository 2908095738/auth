package com.bbs.cache.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.bbs.cache.ThumbCache;
import com.bbs.dto.param.CreateThumbParam;
import com.bbs.enums.RedisKeys;
import com.bbs.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class ThumbCacheImpl implements ThumbCache {

    @Resource
    private RedisUtil redis;

    /**
     * 持久化
     * 点赞：保存redis中：文章id,所属用户id;文章id,<点赞用户id>
     * @param param 点赞参数
     */
    @Override
    public void create(CreateThumbParam param) {
        String json = (String) redis.hashGet(RedisKeys.NEW_THUMB_COMMENT.key() + param.getTcId(), RedisKeys.NEW_THUMB.key());
        Set<Long> userIds = StringUtils.isBlank(json)? new HashSet<>() : JSON.parseObject(json, new TypeReference<Set<Long>>(){});
        userIds.add(param.getUserId());
        redis.hashSet(RedisKeys.NEW_THUMB_COMMENT.key()+param.getTcId(),RedisKeys.NEW_THUMB.key(),JSON.toJSONString(userIds));

        //用户总点赞数+1
        redis.incr(RedisKeys.USER_THUMB.key() + param.getPostUserId(),1);
    }

    /**
     * 查看点赞数量：查询redis
     * 文章点赞数：文章id
     * 用户点赞数：文章id、用户id
     * 评论点赞数：文章id、评论id
     * @param newId 文章id
     * @param userId 用户id
     * @param commentIds 评论id
     * @param type 类型 1文章点赞数，2用户点赞数，3评论点赞数
     * @return Integer
     */
    @Override
    public Integer countBy(Long newId, Long userId, List<Long> commentIds, int type) {
        switch (type){
            case 1:
                return countNewThumb(newId);
            case 2:
                Object json = redis.get(RedisKeys.USER_THUMB.key() + userId);
                return Objects.isNull(json)? 0 : JSON.parseObject(json.toString(), Integer.class);
            case 3:
                return countCommentThumb(newId,commentIds);
        }
        return 0;
    }

    /**
     *取消点赞：删除redis
     */




    //文章点赞数
    private Integer countNewThumb(Long newId) {
        String json = (String) redis.hashGet(RedisKeys.NEW_THUMB_COMMENT.key() + newId, RedisKeys.NEW_THUMB.key());
        Set<Integer> userIds = StringUtils.isBlank(json)? new HashSet<>() : JSON.parseObject(json, new TypeReference<Set<Integer>>(){});
        return userIds.size();
    }

    //评论点赞数
    private Integer countCommentThumb(Long newId, List<Long> commentIds) {
        String json = (String) redis.hashGet(RedisKeys.NEW_THUMB_COMMENT.key() + newId, RedisKeys.COMMENT_THUMB.key()+commentIds);
        List<Set<Integer>> userIds = StringUtils.isBlank(json)? new ArrayList<>(): JSON.parseObject(json, new TypeReference<List<Set<Integer>>>(){});
        return Math.toIntExact(userIds.stream().map(Set::size).count());
    }


}
