package com.bbs.content.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.content.dto.param.UpdateAccountParam;
import com.bbs.content.entity.UserAccount;
import com.bbs.content.dto.GetUserAccountDto;
import com.bbs.entity.UserVO;

/**
 *
 */
public interface UserAccountService extends IService<UserAccount> {

    Long create(UserVO currentUser);

    GetUserAccountDto getByUserId(Long userId);

    void updateAccountByUserId(UpdateAccountParam param);

}
