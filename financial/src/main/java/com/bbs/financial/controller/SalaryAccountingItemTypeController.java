package com.bbs.financial.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.entity.SalaryAccountingItemType;
import com.bbs.financial.service.SalaryAccountingItemTypeService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;
import static com.bbs.Result.success;



/**
 * 核算项目类型Controller
 * @author vctgo
 * @date 2024-05-15
 */
@RestController
public class SalaryAccountingItemTypeController {

    @Resource
    private SalaryAccountingItemTypeService salaryAccountingItemTypeService;

    /**
     * 查询核算项目类型列表
     */
    @GetMapping("/salary/item/type/list")
    public Result<Page<SalaryAccountingItemType>> list(SalaryAccountingItemType salaryAccountingItemType, @RequestParam Integer current, @RequestParam Integer size)
    {
        return success(salaryAccountingItemTypeService.page(new Page<>(current, size), new QueryWrapper<>(salaryAccountingItemType)));
    }


    /**
     * 新增核算项目类型
     */
    @PostMapping("/salary/item/type")
    public Result<Boolean> add(@RequestBody SalaryAccountingItemType salaryAccountingItemType)
    {
        salaryAccountingItemTypeService.save(salaryAccountingItemType);
        return success();
    }

    /**
     * 修改核算项目类型
     */
    @PutMapping("/salary/item/type")
    public Result<Boolean> edit(@RequestBody SalaryAccountingItemType salaryAccountingItemType)
    {
        salaryAccountingItemTypeService.updateById(salaryAccountingItemType);
        return success();
    }


    /**
     * 删除核算项目类型
     */
    @DeleteMapping("/salary/item/type/{ids}")
    public Result<Boolean> remove(@PathVariable List<Long> ids)
    {
        salaryAccountingItemTypeService.getBaseMapper().deleteBatchIds(ids);
        return success();
    }
}