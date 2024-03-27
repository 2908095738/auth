package com.bbs.service.impl;

import com.auth.entity.UserVO;
import com.bbs.cache.ThumbCache;
import com.bbs.converter.UserAccountConverter;
import com.bbs.dto.GetUserAccountDto;
import com.bbs.dto.param.UpdateAccountParam;
import com.bbs.entity.UserAccount;
import com.bbs.mapper.UserAccountMapper;
import com.bbs.service.UserAccountService;
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

    private UserAccountConverter converter;

    private ThumbCache thumbCache;

    @Override
    public Long create(UserVO currentUser) {
        UserAccount userAccount = converter.toEntity(currentUser);
//        userAccount.setAvatarPath();

        return null;
    }


    @Override
    public GetUserAccountDto getByUserId(Long userId) {
        MPJLambdaWrapper<GetUserAccountDto> wrapper = new MPJLambdaWrapper<>();
        GetUserAccountDto result = wrapper.selectAll(UserAccount.class).one();
        result.setLikeCount(thumbCache.countBy(null,userId,null,2));//点赞
        //粉丝result.setFanCount();
        //收藏result.setFavoriteCount();
        //关注result.setFollowerCount();
        //浏览量
        return result;
    }

    @Override
    public void updateAccountByUserId(UpdateAccountParam param) {
        updateById(converter.toEntity(param));
    }

    @Resource
    public void setConverter(UserAccountConverter converter) {
        this.converter = converter;
    }
    @Resource
    public void setThumbCache(ThumbCache thumbCache) {
        this.thumbCache = thumbCache;
    }
}




