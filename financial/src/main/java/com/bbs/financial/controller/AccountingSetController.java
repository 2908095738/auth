package com.bbs.financial.controller;

import com.bbs.Result;
import com.bbs.api.auth.User;
import com.bbs.financial.entity.AccountingSet;
import com.bbs.financial.enums.RedisKeys;
import com.bbs.financial.service.AccountingSetService;
import com.bbs.financial.util.LoginUser;
import com.bbs.financial.util.RedisUtil;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class AccountingSetController {

    @Resource
    private AccountingSetService accountingSetService;

    @Resource
    private RedisUtil redisUtil;


    @GetMapping("/accounting/set/add")
    public Result<Boolean> add(AccountingSet add) {
        return Result.success(accountingSetService.save(add));
    }

    @PostMapping("/accounting/set/update")
    public Result<Boolean> update(AccountingSet update) {
        return Result.success(accountingSetService.updateById(update));
    }

    @DeleteMapping("/accounting/set/delete")
    public Result<Boolean> delete(Long id) {
        return Result.success(accountingSetService.updateDelStatus(id));
    }

    @GetMapping("/accounting/set/list")
    public Result<List<AccountingSet>> list() {
        return Result.success(accountingSetService.getByLoginCompany());
    }

    //把当前选择的账套存到redis中
    @PutMapping("/accounting/set/selected")
    public Result<Boolean> select(Long id) {
        User user = LoginUser.get();
        redisUtil.set(RedisKeys.FINANCIAL_USER_SET.key(user.getId()+"_"+user.getCompanyId()), id);
        return Result.success();
    }

}
