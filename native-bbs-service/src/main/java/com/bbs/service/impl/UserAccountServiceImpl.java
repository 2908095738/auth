package com.bbs.service.impl;

import com.auth.entity.UserVO;
import com.bbs.converter.UserAccountConverter;
import com.bbs.dto.GetUserAccountDto;
import com.bbs.dto.param.UpdateAccountParam;
import com.bbs.entity.Thumb;
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



    @Override
    public Long create(UserVO currentUser) {
        UserAccount userAccount = converter.toEntity(currentUser);
//        userAccount.setAvatarPath();

        return null;
    }


    @Override
    public GetUserAccountDto getByUserId(Long userId) {
        MPJLambdaWrapper<GetUserAccountDto> wrapper = new MPJLambdaWrapper<>();
        wrapper.selectAll(UserAccount.class)
                .selectCount(Thumb::getId,GetUserAccountDto::getLikeCount)
                .leftJoin(Thumb.class,Thumb::getPostUserId,UserAccount::getUserId);
        return wrapper.one();
    }

    @Override
    public void updateAccountByUserId(UpdateAccountParam param) {
        updateById(converter.toEntity(param));
    }

    @Resource
    public void setConverter(UserAccountConverter converter) {
        this.converter = converter;
    }


}




