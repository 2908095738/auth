package com.bbs.content.service.impl;


import com.bbs.content.dto.GetUserAccountDto;
import com.bbs.content.entity.UserAccount;
import com.bbs.content.mapper.UserAccountMapper;
import com.bbs.content.service.UserAccountService;
import com.bbs.content.util.AuthUtil;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class UserAccountServiceImpl extends MPJBaseServiceImpl<UserAccountMapper, UserAccount>
    implements UserAccountService{


    @Override
    public Boolean create(AuthUtil.UserAPI.User user) {
        return save(new UserAccount().setUserId(user.getId()).setNickName(user.getName()).setAvatarPath(user.getPhone()));
    }


    @Override
    public GetUserAccountDto getByUserId(Long userId) {
        GetUserAccountDto result = selectJoinOne(GetUserAccountDto.class,new MPJLambdaWrapper<UserAccount>()
                .selectAll(UserAccount.class)
//                .selectCount(Fan.class,UserAccount::getFanCount)
//                .leftJoin(Fan.class,Fan::getUserId,UserAccount::getUserId)
                .eq(UserAccount::getUserId,userId)
        );
        return result;
    }


}




