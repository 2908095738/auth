package com.bbs.service.impl;

import com.bbs.converter.UserAccountConverter;
import com.bbs.dto.GetUserAccountDto;
import com.bbs.dto.param.UpdateAccountParam;
import com.bbs.entity.UserAccount;
import com.bbs.mapper.UserAccountMapper;
import com.bbs.service.UserAccountService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.query.MPJLambdaQueryWrapper;
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
    public GetUserAccountDto getByUserId(Long userId) {
        return selectJoinOne(GetUserAccountDto.class, new MPJLambdaQueryWrapper<UserAccount>()
                .selectAll(UserAccount.class)
        );
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




