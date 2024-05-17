package com.bbs.financial.controller;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.entity.Account;
import com.bbs.financial.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

import static com.bbs.Result.success;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.*;

/**
 * 科目Controller
 * @author vctgo
 * @date 2024-05-13
 */
@Slf4j
@RestController
@RequestMapping("/account")
public class AccountController
{
    @Resource
    private AccountService accountService;

    private static final int NO_MAX_LENGTH = 4;

    /**
     * 查询科目列表
     */
    @GetMapping("/list")
    public Result<Page<Account>> list(Account account, @RequestParam Integer current, @RequestParam Integer size) {
        String name = account.getName();
        String no = account.getNo();

        String[] split = null;
        if(StringUtils.isNotBlank(no)) {
            if(no.length() > NO_MAX_LENGTH) split = no.split("-");
        }

        account.setNo(null).setName(null);
        return success(accountService.page(new Page<>(current, size), new QueryWrapper<>(account)
                .eq("company_id", INTEGER_ZERO)
                .or().eq(nonNull(account.getCompanyId()), "company_id", account.getCompanyId())
                .or().like(StringUtils.isNotBlank(name), "name", name)
                .or().like(StringUtils.isNotBlank(no), "no", no)
                .or().like(nonNull(split), "level", nonNull(split) ? split.length - 1 : 0)
        ));
    }

    @GetMapping("/tree")
    public Result<List<Tree<Long>>> tree(Account param) {
        return success(accountService.tree(param.getAccountSort(), param.getCompanyId(), param.getName(), param.getNo()));
    }

    @GetMapping("/list/join")
    public Result<Page<Account>> list(
            @RequestParam(required = false) String no,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Long companyId,
            @RequestParam Integer current,
            @RequestParam Integer size
    ) {
        return success(accountService.join(no, name, sort, companyId, current, size));
    }

    /**
     * 获取科目详细信息
     */
    @GetMapping(value = "/{id}")
    public Result<Account> getInfo(@PathVariable("id") Long id)
    {
        return success(accountService.selectAccountById(id));
    }

    /**
     * 新增科目
     */
    @PostMapping
    public Result<Account> add(@RequestBody Account account)
    {
        accountService.insertAccount(account);
        return success(account);
    }

    /**
     * 修改科目
     */
    @PutMapping
    public Result<Boolean> edit(@RequestBody Account account)
    {
        accountService.updateAccount(account);
        return success();
    }

    /**
     * 删除科目
     */
	@DeleteMapping("/{ids}")
    public Result<Boolean> remove(@PathVariable List<Long> ids)
    {
        accountService.deleteAccountByIds(ids);
        return success();
    }

    @GetMapping("/sort")
    public Result<List<String>> searchSort(@RequestParam(required = false) String sort)
    {
        return success(accountService.listObjs(new QueryWrapper<Account>()
                .select("DISTINCT sort")
                .like(StringUtils.isNotBlank(sort), "sort", sort)
                .orderByAsc("sort")
                .isNotNull("sort")
        ));
    }
}
