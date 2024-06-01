package com.bbs.financial.service.impl;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.financial.entity.Account;
import com.bbs.financial.entity.AccountRemark;
import com.bbs.financial.service.AccountService;
import com.bbs.financial.mapper.AccountMapper;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;
import static org.apache.commons.lang3.math.NumberUtils.LONG_ZERO;

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

    @Cacheable(cacheNames = "account-tree")
    @Override
    public List<Tree<Long>> tree(String accountSort, Long companyId, String name, String no) {
        List<Account> allAccount = list(searchAccountWrapper(accountSort, companyId, name, no));
        return tree(allAccount);
    }

    private Wrapper<Account> searchAccountWrapper(String accountSort, Long companyId, String name, String no) {
        return Wrappers.lambdaQuery(Account.class)
                .eq(StringUtils.isNotBlank(accountSort), Account::getAccountSort, accountSort)
                .eq(isNull(companyId), Account::getCompanyId, INTEGER_ZERO)
                .and(nonNull(companyId), wrapper -> wrapper
                        .eq(Account::getCompanyId, INTEGER_ZERO)
                        .or()
                        .eq(Account::getCompanyId, companyId)
                )
                .and((StringUtils.isNotBlank(name) || StringUtils.isNotBlank(no)), wrapper -> wrapper
                        .like(StringUtils.isNotBlank(name), Account::getName, name)
                        .or()
                        .like(StringUtils.isNotBlank(no), Account::getNo, no)
                );
    }

    @Override
    public Page<Account> page(Page<Account> page, String accountSort, Long companyId, String name, String no) {
        return page(page, searchAccountWrapper(accountSort, companyId, name, no));
    }

    @Override
    public List<Tree<Long>> tree(List<Account> accounts) {
        if (accounts.size() > INTEGER_ZERO) {
            TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
            treeNodeConfig.setDeep(5);
            treeNodeConfig.setParentIdKey("parentId");
            treeNodeConfig.setChildrenKey("children");

            return TreeUtil.build(accounts, LONG_ZERO, treeNodeConfig, (account, tree) -> {
                tree.setId(account.getId());
                tree.setParentId(account.getParentId());
                tree.putExtra("label", account.getNo() + " " + account.getName());
                tree.putExtra("accountSort", account.getAccountSort());
                tree.putExtra("direction", account.getDirection());
            });
        }
        return new ArrayList<>();
    }

    @Cacheable(cacheNames = "account-join")
    @Override
    public Page<Account> join(String no, String name, String sort, Long companyId, Integer current, Integer size) {
        return selectJoinListPage(new Page<>(current, size), Account.class, new MPJLambdaWrapper<Account>()
                .selectAll(Account.class)
                .leftJoin(AccountRemark.class, on -> on
                        .eq(AccountRemark::getAccountId, Account::getId)
                        .eq(nonNull(companyId), AccountRemark::getCompanyId, companyId)
                )
                .selectAssociation(AccountRemark.class, Account::getRemark)
                .like(StringUtils.isNotBlank(sort), Account::getSort, sort)
                .or()
                .like(StringUtils.isNotBlank(no), Account::getNo, no)
                .or()
                .like(StringUtils.isNotBlank(name), Account::getName, name)
        );
    }
}