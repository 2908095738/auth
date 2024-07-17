package com.bbs.financial.service;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.financial.entity.Account;
import com.github.yulichang.base.MPJBaseService;

import java.util.List;

/**
* @author 路晨霖
* @description 针对表【account(科目)】的数据库操作Service
* @createDate 2024-05-10 23:34:18
*/
public interface AccountService extends MPJBaseService<Account> {


    /**
     * 查询科目
     *
     * @param id 科目主键
     * @return 科目
     */
    Account selectAccountById(Long id);

    /**
     * 新增科目
     *
     * @param account 科目
     */
    void insertAccount(Account account);

    /**
     * 修改科目
     *
     * @param account 科目
     */
    void updateAccount(Account account);

    /**
     * 批量删除科目
     *
     * @param ids 需要删除的科目主键集合
     */
    void deleteAccountByIds(List<Long> ids);

    List<Tree<Long>> tree(String accountSort, Long accountingSetId, String name, String no);

    Page<Account> page(Page<Account> page, String accountSort, Long accountingSetId, String name, String no);

    List<Tree<Long>> tree(List<Account> accounts);

    Page<Account> join(String no, String name, String sort, Long accountingSetId, Integer current, Integer size);

    List<Tree<Long>> selectTree(Long accountingSetId, String certificateCreateTime);

    List<Tree<Long>> selectQuantityAmountTree(Long accountingSetId, String certificateCreateTime);
}
