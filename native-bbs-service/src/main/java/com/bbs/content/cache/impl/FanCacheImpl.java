package com.bbs.content.cache.impl;

import com.bbs.content.cache.FanCache;

//@Service
public class FanCacheImpl implements FanCache {



//    private RedisUtil redisUtil;

//    @Override
//    public Boolean create(CreateFollowParam param) {
        //查询库里有没有对应的数据
//        //u添加fu为关注
//        addUserFollowOrFan("user_follow_fan:" + param.getUserId(),"user_follow",param.getFollowUserId());
//        //fu添加u为粉丝
//        addUserFollowOrFan("user_follow_fan:" + param.getFollowUserId(),"user_fan",param.getUserId());
//        fanService.create(param);


//        return true;
//    }

//    @Override
//    public List<Fan> getFan(Long userId) {
//        String json = (String)redisUtil.hashGet("user_follow_fan:" + userId, "user_fan");
//        Set<Long> userSet = StringUtils.isBlank(json)? new HashSet<>() : JSON.parseObject(json, new TypeReference<Set<Long>>(){});
//        userSet.stream().map(o-> new Fan()).collect(Collectors.toList())
//        return ;
//    }

//    @Override
//    public List<Fan> getFollow(Long userId) {
//        String json = (String)redisUtil.hashGet("user_follow_fan:" + userId, "user_follow");
//        Set<Long> userSet = StringUtils.isBlank(json)? new HashSet<>() : JSON.parseObject(json, new TypeReference<Set<Long>>(){});
//        userSet.stream().map(o-> new Fan()).collect(Collectors.toList())
//        return ;
//    }

//    @Override
//    public Boolean delFollow(DelFollowParam param) {
        //u添加fu为关注
//        delUserFollowOrFan("user_follow_fan:" + param.getUserId(),"user_follow",param.getFollowUserId());
        //fu添加u为粉丝
//        delUserFollowOrFan("user_follow_fan:" + param.getFollowUserId(),"user_fan",param.getUserId());


//        fanService.delFollow(param);
        //发通知

//        return true;
//    }

//    private void delUserFollowOrFan(String key, String userFollowOrfan, Long userId) {
//        String json = (String)redisUtil.hashGet(key, userFollowOrfan);
//        Set<Long> userSet = StringUtils.isBlank(json)? new HashSet<>() : JSON.parseObject(json, new TypeReference<Set<Long>>(){});
//        userSet.remove(userId);
//        redisUtil.hashSet(key,userFollowOrfan,JSON.toJSONString(userSet));
//    }
//
//    private void addUserFollowOrFan(String key, String userFollowOrfan, Long userId) {
//        String json = (String)redisUtil.hashGet(key, userFollowOrfan);
//        Set<Long> userSet = StringUtils.isBlank(json)? new HashSet<>() : JSON.parseObject(json, new TypeReference<Set<Long>>(){});
//        userSet.add(userId);
//        redisUtil.hashSet(key,userFollowOrfan,JSON.toJSONString(userSet));
//    }




//    @Resource
//    public void setRedisUtil(RedisUtil redisUtil) {
//        this.redisUtil = redisUtil;
//    }
}
