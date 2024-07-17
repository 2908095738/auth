package com.bbs.financial.service.impl;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.financial.entity.Account;
import com.bbs.financial.entity.AccountAuxiliary;
import com.bbs.financial.entity.AccountRemark;
import com.bbs.financial.entity.Certificate;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.mapper.AccountMapper;
import com.bbs.financial.service.AccountService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

//    @Cacheable(cacheNames = "account-tree")
    @Transactional
    @Override
    public List<Tree<Long>> tree(String accountSort, Long accountingSetId, String name, String no) {
        LambdaQueryWrapper<Account> wrapper = searchWrapperByNameOrNo(accountSort, accountingSetId, name, no);
        List<Account> allAccount = list(wrapper);
        List<Account> accountParents = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        for (Account account: allAccount) {
            ids.add(" " + account.getId() + " ");
            accountParents.addAll(searchParents(account));
        }
        accountParents.addAll(searchChildren(ids));
        allAccount.addAll(accountParents);
        return tree(allAccount);
    }

    private List<Account> searchChildren(List<String> ids) {
        return lambdaQuery()
                .like(Account::getParentIds, ids)
                .list();
    }

    private List<Account> searchParents(Account account) {
        String parentIdJSONArr = account.getParentIds();
        List<Account> currentAccountTree = new ArrayList<Account>() {{ add(account);}};
        if(isSetParentIdArrField(parentIdJSONArr)) {
            // 解析父级科目的 parentIds 字段，查询父级科目链，并填充到 currentAccountTree
            currentAccountTree.addAll(searchByIds(parentIdJSONArr));
        } else {
            List<Long> parentIds = new ArrayList<>();
            // 递归查询父级科目，并填充到 currentAccountTree
            recursiveSearchParentAndFillToList(account, currentAccountTree, parentIds);
            // 更新当前科目的 parentIds 字段
            update(joinParentIdJSONArray(account, parentIds), account);
        }
        return currentAccountTree;
    }

    private void update(String parentIdJSONArr, Account account) {
        lambdaUpdate().set(Account::getParentIds, parentIdJSONArr).eq(Account::getId, account.getId()).update();
    }

    private String joinParentIdJSONArray(Account account, List<Long> parentIds) {
        List<String> ids = new ArrayList<String>() {{ add(" " + account.getId() + " "); }};
        parentIds.forEach(parentId -> ids.add(" " + parentId + " "));
        return JSONArray.toJSONString(ids);
    }

    private Boolean isSetParentIdArrField(String parentIdJSONArr) {
        return StringUtils.isNotBlank(parentIdJSONArr);
    }

    private List<Account> searchByIds(String idJSONArr) {
        List<String> ids = JSONArray.parseArray(idJSONArr, String.class);
        List<String> notContainCurrentAccountIdList = ids.subList(INTEGER_ZERO, ids.size());
        return listByIds(notContainCurrentAccountIdList.stream()
                .map(String::trim)
                .map(Long::valueOf)
                .collect(Collectors.toList()));
    }

    private void recursiveSearchParentAndFillToList(Account currentAccount, List<Account> parents, List<Long> parentIds) {
        if(currentAccount.getLevel() > INTEGER_ZERO) {
            Account parent = getById(currentAccount.getParentId());
            parents.add(parent);
            parentIds.add(parent.getId());
            recursiveSearchParentAndFillToList(parent, parents, parentIds);
        }
    }

    private LambdaQueryWrapper<Account> searchWrapperByNameOrNo(String accountSort, Long accountingSetId, String name, String no) {
        LambdaQueryWrapper<Account> wrapper = baseWrapper(accountSort, accountingSetId);
        // 如果 param.name 为 no，而且并 param.no 为 null，则使用 name 的值，作为 no 字段查询
        if(StringUtils.isNotBlank(name)) {
            if(isNumber(name)) {
                // param.name != null && param.no == null
                if (StringUtils.isBlank(no)) {
                    wrapper = wrapper.and(ext -> ext
                                    .eq(Account::getNo, name)
                                    .or()
                                    .like(Account::getName, name)
                    );
                } else {
                    // param.name != null && param.no != null
                    wrapper = wrapper.and(ext -> ext
                            .and(ext2 -> ext2
                                    .eq(StringUtils.isNotBlank(no), Account::getNo, no)
                                    .or()
                                    .like(Account::getName, name)
                            )
                    );
                }
            } else {
                // 如果 name 为正常字符串，则先 like，取出结果集的 no 去重，再查询对应 no 的全部科目
                wrapper = wrapper
                        .and(ext -> ext
                                .in(Account::getNo, no)
                                .or()
                                .like(Account::getName, name)
                        );
            }
        } else {
            wrapper = wrapper.eq(StringUtils.isNotBlank(no), Account::getNo, no);
        }
        return wrapper;
    }

    private boolean isNumber(String name) {
        return nonNull(name) && name.matches("-?\\d+(\\.\\d+)?");
    }


    private LambdaQueryWrapper<Account> baseWrapper(String accountSort, Long accountingSetId) {
        return Wrappers.lambdaQuery(Account.class)
                .eq(StringUtils.isNotBlank(accountSort), Account::getAccountSort, accountSort)
                .eq(isNull(accountingSetId), Account::getAccountingSetId, INTEGER_ZERO)
                .and(nonNull(accountingSetId), wrapper -> wrapper
                        .eq(Account::getAccountingSetId, INTEGER_ZERO)
                        .or()
                        .eq(Account::getAccountingSetId, accountingSetId)
                );
    }

    @Override
    public Page<Account> page(Page<Account> page, String accountSort, Long accountingSetId, String name, String no) {
        return page(page, searchWrapperByNameOrNo(accountSort, accountingSetId, name, no));
    }

    @Override
    public List<Tree<Long>> tree(List<Account> accounts) {
        if (accounts.size() > INTEGER_ZERO) {
            TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
            treeNodeConfig.setDeep(5);
            treeNodeConfig.setParentIdKey("parentId");
            treeNodeConfig.setChildrenKey("children");

            return TreeUtil.build(accounts, LONG_ZERO, treeNodeConfig, (account, tree) -> {
                if(account!=null){
                    tree.setId(account.getId());
                    tree.setParentId(account.getParentId());
                    tree.putExtra("label", account.getNo() + " " + account.getName());
                    tree.putExtra("accountSort", account.getAccountSort());
                    tree.putExtra("direction", account.getDirection());
                    tree.putExtra("account", account);
                }
            });
        }
        return new ArrayList<>();
    }

    @Cacheable(cacheNames = "account-join")
    @Override
    public Page<Account> join(String no, String name, String sort, Long accountingSetId, Integer current, Integer size) {
        return selectJoinListPage(new Page<>(current, size), Account.class, new MPJLambdaWrapper<Account>()
                .selectAll(Account.class)
                .leftJoin(AccountRemark.class, on -> on
                        .eq(AccountRemark::getAccountId, Account::getId)
                        .eq(nonNull(accountingSetId), AccountRemark::getAccountingSetId, accountingSetId)
                )
                .selectAssociation(AccountRemark.class, Account::getRemark)
                .like(StringUtils.isNotBlank(sort), Account::getSort, sort)
                .or()
                .like(StringUtils.isNotBlank(no), Account::getNo, no)
                .or()
                .like(StringUtils.isNotBlank(name), Account::getName, name)
        );
    }

    @Override
    public List<Tree<Long>> selectTree(Long accountingSetId, String certificateCreateTime) {
        List<Account> accountList = selectJoinList(Account.class, new MPJLambdaWrapper<Account>()
                .selectAll(Account.class)
                .leftJoin(CertificateAbstract.class, CertificateAbstract::getAccountId, Account::getId)
                .leftJoin(Certificate.class, Certificate::getId, CertificateAbstract::getCertificateId)
                .eq(Certificate::getAccountingSetId,accountingSetId)
                .like(Certificate::getCreateTime,certificateCreateTime)
        );
        List<Account> result = accountList;
        if (accountList.size() > INTEGER_ZERO) {
            //去重
            accountList = accountList.stream().distinct().collect(Collectors.toList());
            //根据每个科目的父级id及层级，查出所有管联数据
            for (Account account : accountList) {
                if(account.getLevel()>0){
                    //当前数据的所有同类别科目
                    List<Account> list = list(Wrappers.lambdaQuery(Account.class)
                            .eq(Account::getNo, account.getNo())
                    );
                    //过滤出小于当前层级的数据
                    result.addAll(list.stream().filter(account1 -> account1.getLevel() < account.getLevel()).collect(Collectors.toList()));
                }
            }
        }
        return tree(result);
    }

    @Override
    public List<Tree<Long>> selectQuantityAmountTree(Long accountingSetId, String certificateCreateTime) {
        List<Account> accountList = selectJoinList(Account.class, new MPJLambdaWrapper<Account>()
                .selectAll(Account.class)
                .selectCollection(AccountAuxiliary.class,Account::getAccountAuxiliaryList)
                .leftJoin(CertificateAbstract.class, CertificateAbstract::getAccountId, Account::getId)
                .leftJoin(Certificate.class, Certificate::getId, CertificateAbstract::getCertificateId)
                .leftJoin(AccountAuxiliary.class, on -> on
                        .eq(AccountAuxiliary::getId, CertificateAbstract::getAccountId)
                        .eq(AccountAuxiliary::getName, accountingSetId)
                        .eq(nonNull(accountingSetId), AccountAuxiliary::getAccountingSetId, accountingSetId)
                )
                .eq(Account::getQuantitativeAccount,"是")
                .eq(Certificate::getAccountingSetId,accountingSetId)
                .like(Certificate::getCreateTime,certificateCreateTime)
        );
        List<Tree<Long>> trees = new ArrayList<>();
        if (accountList.size() > INTEGER_ZERO) {
            for (Account account : accountList) {
                Tree<Long> tree = new Tree<>();
                tree.setId(account.getId());
                tree.setParentId(0L);
                tree.putExtra("label", account.getNo() + " " + account.getName());
                tree.putExtra("accountSort", account.getAccountSort());
                tree.putExtra("direction", account.getDirection());
                tree.putExtra("account", account);

                List<Tree<Long>> treeChildren = new ArrayList<>();

                List<AccountAuxiliary> accountAuxiliaryList = account.getAccountAuxiliaryList();
                if (accountAuxiliaryList.size() > INTEGER_ZERO) {
                    //把accountAuxiliaryList转成数结构
                    accountAuxiliaryList.forEach(accountAuxiliary -> {
                        Tree<Long> child = new Tree<>();
                        child.setId(accountAuxiliary.getId());
                        child.putExtra("label", accountAuxiliary.getNo() + " " + accountAuxiliary.getName());
                        child.putExtra("accountAuxiliary", accountAuxiliary);
                        treeChildren.add(child);
                    });
                    tree.setChildren(treeChildren);
                }
                trees.add(tree);
            }
        }
        return trees;
    }


}