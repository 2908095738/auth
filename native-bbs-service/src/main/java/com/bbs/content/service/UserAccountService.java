package com.bbs.content.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.content.dto.GetUserAccountDto;
import com.bbs.content.entity.UserAccount;
import com.bbs.content.util.AuthUtil;

/**
 *
 */
public interface UserAccountService extends IService<UserAccount> {

    Boolean create(AuthUtil.UserAPI.User user);

    GetUserAccountDto getByUserId(Long userId);


}
