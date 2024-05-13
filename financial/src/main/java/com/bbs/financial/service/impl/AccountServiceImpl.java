package com.bbs.financial.service.impl;

import com.bbs.financial.entity.Account;
import com.bbs.financial.service.AccountService;
import com.bbs.financial.mapper.AccountMapper;
import com.github.yulichang.base.MPJBaseServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 路晨霖
* @description 针对表【account(科目)】的数据库操作Service实现
* @createDate 2024-05-10 23:34:18
*/
@Service
public class AccountServiceImpl extends MPJBaseServiceImpl<AccountMapper, Account> implements AccountService{


    /**
     * 查询科目
     *
     * @param id 科目主键
     * @return 科目
     */
    @Override
    public Account selectAccountById(Long id)
    {
        return getById(id);
    }

    /**
     * 新增科目
     *
     * @param account 科目
     */

    @Override
    public void insertAccount(Account account)
    {
        save(account);
    }

    /**
     * 修改科目
     *
     * @param account 科目
     */
    @Override
    public void updateAccount(Account account)
    {
        updateById(account);
    }

    /**
     * 批量删除科目
     *
     * @param ids 需要删除的科目主键
     */
    @Override
    public void deleteAccountByIds(List<Long> ids)
    {
        baseMapper.deleteBatchIds(ids);
    }
}