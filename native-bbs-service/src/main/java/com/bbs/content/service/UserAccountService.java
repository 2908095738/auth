package com.bbs.content.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.content.dto.GetUserAccountDto;
import com.bbs.content.entity.UserAccount;

/**
 *
 */
public interface UserAccountService extends IService<UserAccount> {

    Boolean create(Long userId);

    GetUserAccountDto getByUserId(Long userId);


}
