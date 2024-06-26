package com.bbs.financial.controller;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.entity.*;
import com.bbs.financial.service.AccountCurrencyService;
import com.bbs.financial.service.AccountService;
import com.bbs.financial.service.AuxiliaryCalculationService;
import com.bbs.financial.service.PriceTypeService;
import com.bbs.financial.util.LoginUser;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

import static com.bbs.Result.failed;
import static com.bbs.Result.success;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.*;

/**
 * 科目Controller
 * @author vctgo
 * @date 2024-05-13
 */
@Slf4j
@RestController
@RequestMapping
public class AccountController
{
    @Resource
    private AccountService accountService;

    private static final int NO_MAX_LENGTH = 4;

    /**
     * 查询科目列表
     */
    @GetMapping("/account/list")
    public Result<Page<Account>> list(Account param, @RequestParam Integer current, @RequestParam Integer size) {
        if(Objects.equals(param.getAccountSort(), "全部")) {
            param.setAccountSort(null);
        }
        return success(accountService.page(new Page<>(current, size),
                param.getAccountSort(),
                param.getCompanyId(),
                param.getName(),
                param.getNo()
        ));
    }

    private Tree<Long> loop(String[] noStrArr, Tree<Long> result, Integer level) {
        List<Tree<Long>> children = result.getChildren();
        if(nonNull(children) && children.size() > INTEGER_ZERO) {   // 是否存在下一层科目 1
            int currentLevel = level + INTEGER_ONE;
            String indexStr = noStrArr[currentLevel];

            // 清除 xxx-01-xxx 中 01 的 0
            if(indexStr.charAt(INTEGER_ZERO) == '0') {
                //前端输入最小值 xxxx-01 or xxxx-1，不能为 0
                Preconditions.checkArgument(indexStr.length() > INTEGER_ONE, "子级序号不能为 0，最小为 1");
                // 如果格式为 xx-0-xx ，即 0 则直接返回 0
                if(indexStr.length() > 2) {
                    indexStr = indexStr.substring(INTEGER_ONE);
                }
            }
            // 需要 -1 对应下标
            result = children.get(Integer.parseInt(indexStr) - INTEGER_ONE);  // 获取下一层科目（第 level+1 层）

            if(nonNull(result)) {   // 是否存在下一层科目 2
                result = loop(noStrArr, result, currentLevel);
            }
        }
        return result;
    }

    @GetMapping("/account/tree")
    public Result<List<Tree<Long>>> tree(Account param) {
        String no = param.getNo();
        boolean isSearchNo = StringUtils.isNotBlank(no) && no.length() > NO_MAX_LENGTH + INTEGER_ONE;
        String[] split = null;

        if(isSearchNo) {
            split = no.split("-");
            param.setNo(split[INTEGER_ZERO]);
        }

        if(Objects.equals(param.getAccountSort(), "全部")) {
            param.setAccountSort(null);
        }

        List<Tree<Long>> tree = accountService.tree(param.getAccountSort(), param.getCompanyId(), param.getName(), param.getNo());

        if(isSearchNo) {
            Tree<Long> result = tree.get(INTEGER_ZERO);
            if(nonNull(result)) {
                result = loop(split, result, INTEGER_ZERO);
            }
            return success(Collections.singletonList(result));
        } else {
            return success(tree);
        }
    }

    @GetMapping("/account/join")
    public Result<Account> one(@RequestParam Long id) {
        return success(accountService.selectJoinOne(Account.class, new MPJLambdaWrapper<Account>()
                .selectAll(Account.class)
                .leftJoin(AccountRemark.class, AccountRemark::getAccountId, Account::getId, ext -> ext
                        .selectAssociation(AccountRemark.class, Account::getRemark)
                )
                .eq(Account::getId, id)
        ));
    }

    @GetMapping("/account/list/join")
    @Cacheable(cacheNames = "account:list:join")
    public Result<Page<Account>> list(
            @RequestParam(required = false) String no,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Long companyId,
            @RequestParam Integer current,
            @RequestParam Integer size
    ) {

        boolean isSearchNo = StringUtils.isNotBlank(no) && no.indexOf('-') > -INTEGER_ONE;
        String[] split = null;

        if(isSearchNo) {
            split = no.split("-");
            no = split[INTEGER_ZERO];
        }

        String finalNo = no;
        String[] finalSplit = split;
        int level = isSearchNo ? finalSplit.length - INTEGER_ONE : INTEGER_ZERO;
        int parentLevel = level - INTEGER_ONE;
        Page<Account> page = accountService.selectJoinListPage(new Page<>(current, size), Account.class, new MPJLambdaWrapper<Account>()
                .selectAll(Account.class)
                .leftJoin(AccountRemark.class, on -> on
                        .eq(AccountRemark::getAccountId, Account::getId)
                        .eq(nonNull(companyId), AccountRemark::getCompanyId, companyId)
                )
                .selectAssociation(AccountRemark.class, Account::getRemark)
                .and(
                        StringUtils.isNotBlank(sort) || isSearchNo || StringUtils.isNotBlank(finalNo) || StringUtils.isNotBlank(name),
                        wrapper -> wrapper
                            .eq(StringUtils.isNotBlank(sort), Account::getSort, sort)
                            // 如果需要查询子级：同时查具体科目与直接父级科目（用于判断父级是否存在）
                            .in(isSearchNo, Account::getLevel, level, parentLevel)
                            .and(wrapper2 -> wrapper2
                                    // 如果搜索格式为 xxx-xx，则精确匹配 no（后续代码再筛选子级），否则模糊匹配 no
                                    .eq(StringUtils.isNotBlank(finalNo) && isSearchNo, Account::getNo, finalNo)
                                    .likeRight(StringUtils.isNotBlank(finalNo) && !isSearchNo, Account::getNo, finalNo)
                                    .or(StringUtils.isNotBlank(name)).like(StringUtils.isNotBlank(name), Account::getName, name)
                            )
                )
        );
        if(isSearchNo) {
            // 根据 no 中的 - 的数量，获取科目序号
            String indexStr = split[split.length - INTEGER_ONE];

            // 清除 xxx-01-xxx 中 01 的 0
            if(indexStr.charAt(INTEGER_ZERO) == '0') {

                //前端输入最小值 xxxx-01 or xxxx-1，不能为 0
                Preconditions.checkArgument(indexStr.length() > INTEGER_ONE, "子级序号不能为 0，最小为 1");

                // 获取前端输入的序号（xxxx-01-xxx 的 1）
                indexStr = indexStr.substring(INTEGER_ONE);

            }

            // 封装 Page
            Page<Account> result = new Page<>(current, INTEGER_ONE);
            int index = Integer.parseInt(indexStr);
            // 从中子级科目，过滤出目标科目（对应序号 xxx-01）和父级科目
            Account targetAccount = null;
            Account parentAccount = null;
            List<Account> accountList = page.getRecords();
            for (Account account : accountList) {
                if (account.getWeight() == index) targetAccount = account;
                if (account.getLevel() == parentLevel) parentAccount = account;
            }
            if(isNull(parentAccount)) return failed(400, "父级科目不存在，请先创建父级科目");
            // 如果没用对应序号的子级科目，则返回空数组
            result.setRecords(nonNull(targetAccount) ? Collections.singletonList(targetAccount) : new ArrayList<>());
            return success(result);
        }
        return success(page);
    }

    /**
     * 获取科目详细信息
     */
    @GetMapping(value = "/account/{id}")
    public Result<Account> getInfo(@PathVariable("id") Long id)
    {
        return success(accountService.selectAccountById(id));
    }

    /**
     * 新增科目
     */
//    @PutMapping("/account")
    public Result<Account> add(@RequestBody Account account)
    {
        accountService.insertAccount(account);
        return success(account);
    }

    /**
     * 修改科目
     */
    @PostMapping("/account")
    public Result<Boolean> edit(@RequestBody Account account)
    {
        accountService.updateAccount(account);
        return success();
    }

    /**
     * 删除科目
     */
	@DeleteMapping("/account/{ids}")
    public Result<Boolean> remove(@PathVariable List<Long> ids)
    {
        accountService.deleteAccountByIds(ids);
        return success();
    }

    @GetMapping("/account/sort")
    public Result<List<String>> searchSort(@RequestParam(required = false) String sort)
    {
        return success(accountService.listObjs(new QueryWrapper<Account>()
                .select("DISTINCT sort")
                .like(StringUtils.isNotBlank(sort), "sort", sort)
                .orderByAsc("sort")
                .isNotNull("sort")
        ));
    }

    @Resource
    private AccountCurrencyService accountCurrencyService;

    @PutMapping("/account/currency")
    public Result<Boolean> addCurrency(@RequestBody AccountCurrency currency)
    {
        boolean exists = accountCurrencyService.lambdaQuery().eq(AccountCurrency::getCode, currency.getCode()).exists();
        if(exists) return failed(400, "创建失败，币种已存在");
        currency.setCreateBy(LoginUser.getId());
        return success(accountCurrencyService.save(currency));
    }

    @GetMapping("/account/currency/list")
    public Result<List<AccountCurrency>> searchCurrencyList(@RequestParam Long companyId) {
        return success(accountCurrencyService.lambdaQuery().eq(AccountCurrency::getCompanyId, companyId).list());
    }

    @GetMapping("/account/name")
    public Result<List<String>> searchByName(@RequestParam(required = false) String name) {
        return success(accountService.listObjs(new LambdaQueryWrapper<Account>()
                .select(Account::getName)
                .like(StringUtils.isNotBlank(name), Account::getName, name)
        ));
    }

    @Resource
    private PriceTypeService priceTypeService;

    @PutMapping("/money/type")
    public Result<Boolean> addMoneyType(@RequestBody PriceType priceType)
    {
        if(priceTypeService.lambdaQuery()
                .eq(PriceType::getCode, priceType.getCode())
                .eq(PriceType::getCompanyId, priceType.getCompanyId())
                .exists()) {
            return failed(400, "创建失败，币种已存在");
        }
        priceType.setCreateBy(LoginUser.getId());
        return success(priceTypeService.save(priceType));
    }

    @GetMapping("/money/type/list")
    public Result<List<PriceType>> searchMoneyTypeList(@RequestParam Long companyId) {
        return success(priceTypeService.lambdaQuery().eq(PriceType::getCompanyId, companyId).list());
    }

    @Resource
    private AuxiliaryCalculationService auxiliaryCalculationService;

    @PutMapping("/auxiliary/calculation")
    public Result<Boolean> addAuxiliaryCalculation(@RequestBody AuxiliaryCalculation auxiliaryCalculation) {
        if(auxiliaryCalculationService.lambdaQuery()
                .eq(AuxiliaryCalculation::getCompanyId, auxiliaryCalculation.getCompanyId())
                .eq(AuxiliaryCalculation::getName, auxiliaryCalculation.getName()).exists()) {
            return failed(400, "创建失败，辅助核算项已存在");
        }
        return success(auxiliaryCalculationService.save(auxiliaryCalculation));
    }

    @GetMapping("/auxiliary/calculation/list")
    public Result<List<AuxiliaryCalculation>> searchAuxiliaryCalculationList(@RequestParam Long companyId) {
        return success(auxiliaryCalculationService.lambdaQuery().eq(AuxiliaryCalculation::getCompanyId, companyId).list());
    }
}
