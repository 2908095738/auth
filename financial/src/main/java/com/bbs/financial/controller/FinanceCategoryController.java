package com.bbs.financial.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.entity.FinanceCategory;
import com.bbs.financial.service.FinanceCategoryService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static com.bbs.Result.success;

/**
 * 资产类别Controller
 * @author vctgo
 * @date 2024-05-14
 */
@RestController
@RequestMapping("/category")
public class FinanceCategoryController {

    @Resource
    private FinanceCategoryService financeCategoryService;

    /**
     * 查询资产类别列表
     */
//    @RequiresPermissions("system:category:list")
    @GetMapping("/list")
    public Result<Page<FinanceCategory>> list(FinanceCategory financeCategory, @RequestParam Integer current, @RequestParam Integer size)
    {
        return success(financeCategoryService.page(new Page<>(current, size), new QueryWrapper<>(financeCategory)));
    }

    /**
     * 获取资产类别详细信息
     */
//    @RequiresPermissions("system:category:query")
    @GetMapping(value = "/{id}")
    public Result<FinanceCategory> getInfo(@PathVariable("id") Long id)
    {
        return success(financeCategoryService.getById(id));
    }

    /**
     * 新增资产类别
     */
//    @RequiresPermissions("system:category:add")
//    @Log(title = "资产类别", businessType = BusinessType.INSERT)
    @PostMapping
    public Result<Boolean> add(@RequestBody FinanceCategory financeCategory)
    {
        financeCategoryService.save(financeCategory);
        return success();
    }

    /**
     * 修改资产类别
     */
//    @RequiresPermissions("system:category:edit")
//    @Log(title = "资产类别", businessType = BusinessType.UPDATE)
    @PutMapping
    public Result<Boolean> edit(@RequestBody FinanceCategory financeCategory)
    {
        financeCategoryService.updateById(financeCategory);
        return success();
    }

    /**
     * 删除资产类别
     */
//    @RequiresPermissions("system:category:remove")
    //   @Log(title = "资产类别", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public Result<Boolean> remove(@PathVariable List<Long> ids)
    {
        financeCategoryService.getBaseMapper().deleteBatchIds(ids);
        return success();
    }
}