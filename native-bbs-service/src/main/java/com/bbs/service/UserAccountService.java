package com.bbs.service;

import com.bbs.dto.GetUserAccountDto;
import com.bbs.dto.param.UpdateAccountParam;
import com.bbs.entity.UserAccount;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 *
 */
public interface UserAccountService extends IService<UserAccount> {

    GetUserAccountDto getByUserId(Long userId);

    void updateAccountByUserId(UpdateAccountParam param);
}
