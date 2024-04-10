package com.bbs.auth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.auth.app.follow.DelFollow;
import com.bbs.auth.app.follow.Follow;
import com.bbs.auth.converter.FanConverter;
import com.bbs.auth.entity.Fan;
import com.bbs.auth.mapper.FanMapper;
import com.bbs.auth.service.FanService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 *
 */
@Service
public class FanServiceImpl extends ServiceImpl<FanMapper, Fan> implements FanService {

    private FanConverter fanConverter;

    @Override
        public Boolean create(Follow.Param param) {
            return save(fanConverter.toEntity(param));
        }

    @Override
    public Boolean delFollow(DelFollow.Param param) {
        return lambdaUpdate().set(Fan::getDeleteFlag,1).eq(Fan::getUserId,param.getUserId()).eq(Fan::getFollowUserId,param.getFollowUserId()).update();
    }



    @Override
    public List<Fan> getFollow(Long userId) {
        return lambdaQuery().eq(Fan::getDeleteFlag,0).eq(Fan::getUserId,userId).list();
    }

    @Override
    public List<Fan> getFan(Long userId) {
        return lambdaQuery().eq(Fan::getDeleteFlag,0).eq(Fan::getFollowUserId,userId).list();
    }


    @Resource
    public void setFanConverter(FanConverter fanConverter) {
        this.fanConverter = fanConverter;
    }
}




