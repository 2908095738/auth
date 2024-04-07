package com.bbs.content.service.impl;


import com.bbs.content.cache.ThumbCache;
import com.bbs.content.dto.GetUserAccountDto;
import com.bbs.content.entity.UserAccount;
import com.bbs.content.mapper.UserAccountMapper;
import com.bbs.content.service.UserAccountService;
import com.bbs.content.util.AuthUtil;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 *
 */
@Service
public class UserAccountServiceImpl extends MPJBaseServiceImpl<UserAccountMapper, UserAccount>
    implements UserAccountService{

    private ThumbCache thumbCache;

    @Override
    public Boolean create(AuthUtil.UserAPI.User user) {
        return save(new UserAccount().setUserId(user.getUserId()).setNickName(user.getNickName()).setAvatarPath(user.getAvatar()));
    }


    @Override
    public GetUserAccountDto getByUserId(Long userId) {
        GetUserAccountDto result = selectJoinOne(GetUserAccountDto.class,new MPJLambdaWrapper<UserAccount>()
                .selectAll(UserAccount.class)
//                .selectCount(Fan.class,UserAccount::getFanCount)
//                .leftJoin(Fan.class,Fan::getUserId,UserAccount::getUserId)
                .eq(UserAccount::getUserId,userId)
        );
        result.setLikeCount(thumbCache.countBy(null,userId,null,2));//点赞
        //粉丝result.setFanCount();
        //收藏result.setFavoriteCount();
        //关注result.setFollowerCount();
        //浏览量
        return result;
    }

    @Resource
    public void setThumbCache(ThumbCache thumbCache) {
        this.thumbCache = thumbCache;
    }


}




