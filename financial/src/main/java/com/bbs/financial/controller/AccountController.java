package com.bbs.financial.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.entity.Account;
import com.bbs.financial.entity.AccountRemark;
import com.bbs.financial.service.AccountService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static com.bbs.Result.success;
import static java.util.Objects.nonNull;

/**
 * 科目Controller
 * @author vctgo
 * @date 2024-05-13
 */
@RestController
@RequestMapping("/account")
public class AccountController
{
    @Resource
    private AccountService accountService;

    /**
     * 查询科目列表
     */
//    @RequiresPermissions("system:account:list")
    @GetMapping("/list")
    public Result<Page<Account>> list(Account account, @RequestParam Integer current, @RequestParam Integer size) {
        return success(accountService.page(new Page<>(current, size), new QueryWrapper<>(account)));
    }

    @GetMapping("/list/join")
    public Result<Page<Account>> list(
            @RequestParam(required = false) String no,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long companyId,
            @RequestParam Integer current,
            @RequestParam Integer size
    ) {
        return success(accountService.selectJoinListPage(new Page<>(current, size), Account.class, new MPJLambdaWrapper<Account>()
                .selectAll(Account.class)
                .leftJoin(AccountRemark.class, AccountRemark::getAccountId, Account::getId, ext -> ext
                        .selectAssociation(AccountRemark.class, Account::getRemark)
                )
                .like(StringUtils.isNotBlank(no), Account::getNo, no)
                .like(StringUtils.isNotBlank(name), Account::getName, name)
                .eq(nonNull(companyId), AccountRemark::getCompanyId, companyId)
        ));
    }

    /**
     * 获取科目详细信息
     */
//    @RequiresPermissions("system:account:query")
    @GetMapping(value = "/{id}")
    public Result<Account> getInfo(@PathVariable("id") Long id)
    {
        return success(accountService.selectAccountById(id));
    }

    /**
     * 新增科目
     */
//    @RequiresPermissions("system:account:add")
//    @Log(title = "科目", businessType = BusinessType.INSERT)
    @PostMapping
    public Result<Account> add(@RequestBody Account account)
    {
        accountService.insertAccount(account);
        return success(account);
    }

    /**
     * 修改科目
     */
//    @RequiresPermissions("system:account:edit")
//    @Log(title = "科目", businessType = BusinessType.UPDATE)
    @PutMapping
    public Result<Boolean> edit(@RequestBody Account account)
    {
        accountService.updateAccount(account);
        return success();
    }

    /**
     * 删除科目
     */
//    @RequiresPermissions("system:account:remove")
//    @Log(title = "科目", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public Result<Boolean> remove(@PathVariable List<Long> ids)
    {
        accountService.deleteAccountByIds(ids);
        return success();
    }
}
