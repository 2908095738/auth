package com.bbs.content.cache.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.bbs.content.cache.ThumbCache;
import com.bbs.content.dto.param.CreateThumbParam;
import com.bbs.content.util.RedisUtil;
import com.bbs.content.dto.param.CancelThumbParam;
import com.bbs.content.enums.RedisKeys;
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
     * 如果评论id有数据，就存到评论中
     * 点赞类型：1文章2文章下的评论
     * @param param 点赞参数
     */
    @Override
    public void create(CreateThumbParam param) {
        if (param.getType()==1) {
            addThumb(RedisKeys.NEW_THUMB_COMMENT.getPrefix() + param.getNewId(), RedisKeys.NEW_THUMB.getPrefix(), param.getUserId());
        }else if(param.getType()==2){
            addThumb(RedisKeys.NEW_THUMB_COMMENT.getPrefix() + param.getNewId(),RedisKeys.COMMENT_THUMB.getPrefix()+param.getCommentId(),param.getUserId());
        }
        //用户总点赞数+1
        redis.incr(RedisKeys.USER_THUMB.getPrefix() + param.getPostUserId(),1);
    }


    /**
     * 添加点赞缓存
     * @param newKey 内容Key
     * @param fKey 内容或评论Key
     * @param userId 点赞用户
     */
    private void addThumb(String newKey, String fKey, Long userId) {
        String json = (String) redis.hashGet(newKey,  fKey);
        Set<Long> userIds = StringUtils.isBlank(json)? new HashSet<>() : JSON.parseObject(json, new TypeReference<Set<Long>>(){});
        userIds.add(userId);
        redis.hashSet(newKey, fKey,JSON.toJSONString(userIds));
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
                Object json = redis.get(RedisKeys.USER_THUMB.getPrefix() + userId);
                return Objects.isNull(json)? 0 : JSON.parseObject(json.toString(), Integer.class);
            case 3:
                return countCommentThumb(newId,commentIds);
        }
        return 0;
    }


    /**
     *取消点赞：删除redis
     * 判断类型
     */
    @Override
    public void cancel(CancelThumbParam param) {
        if (param.getType()==1) {
            delThumb(RedisKeys.NEW_THUMB_COMMENT.getPrefix() + param.getNewId(),RedisKeys.NEW_THUMB.getPrefix(),param.getUserId());
        }else if(param.getType()==2){
            addThumb(RedisKeys.NEW_THUMB_COMMENT.getPrefix() + param.getNewId(),RedisKeys.COMMENT_THUMB.getPrefix()+param.getCommentId(),param.getUserId());
        }
        //用户总点赞数-1
        redis.incr(RedisKeys.USER_THUMB.getPrefix() + param.getPostUserId(),-1);
    }

    /**
     * 删除点赞缓存
     * @param newKey 内容Key
     * @param fKey 内容或评论Key
     * @param userId 点赞用户
     */
    private void delThumb(String newKey, String fKey, Long userId) {
        String json = (String) redis.hashGet(newKey, fKey);
        Set<Long> userIds = StringUtils.isBlank(json)? new HashSet<>() : JSON.parseObject(json, new TypeReference<Set<Long>>(){});
        if(CollUtil.isNotEmpty(userIds))
            userIds.remove(userId);
        redis.delHash(newKey,fKey);
    }



    //文章点赞数
    private Integer countNewThumb(Long newId) {
        String json = (String) redis.hashGet(RedisKeys.NEW_THUMB_COMMENT.getPrefix()+ newId, RedisKeys.NEW_THUMB.getPrefix());
        Set<Integer> userIds = StringUtils.isBlank(json)? new HashSet<>() : JSON.parseObject(json, new TypeReference<Set<Integer>>(){});
        return userIds.size();
    }

    //评论点赞数
    private Integer countCommentThumb(Long newId, List<Long> commentIds) {
        String json = (String) redis.hashGet(RedisKeys.NEW_THUMB_COMMENT.getPrefix() + newId, RedisKeys.COMMENT_THUMB.getPrefix()+commentIds);
        List<Set<Integer>> userIds = StringUtils.isBlank(json)? new ArrayList<>(): JSON.parseObject(json, new TypeReference<List<Set<Integer>>>(){});
        return Math.toIntExact(userIds.stream().map(Set::size).count());
    }


}
