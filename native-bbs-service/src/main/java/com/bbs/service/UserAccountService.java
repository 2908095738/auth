package com.bbs.service;

import com.auth.entity.UserVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.dto.GetUserAccountDto;
import com.bbs.dto.param.UpdateAccountParam;
import com.bbs.entity.UserAccount;

/**
 *
 */
public interface UserAccountService extends IService<UserAccount> {

    Long create(UserVO currentUser);

    GetUserAccountDto getByUserId(Long userId);

    void updateAccountByUserId(UpdateAccountParam param);

}
