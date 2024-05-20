package com.bbs.financial.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.entity.AuxiliaryCalculation;
import com.bbs.financial.service.AuxiliaryCalculationService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    private AuxiliaryCalculationService salaryAccountingItemTypeService;

    /**
     * 查询核算项目类型列表
     */
    @GetMapping("/salary/item/type/list")
    public Result<Page<AuxiliaryCalculation>> list(AuxiliaryCalculation salaryAccountingItemType, @RequestParam Integer current, @RequestParam Integer size)
    {
        return success(salaryAccountingItemTypeService.page(new Page<>(current, size), new QueryWrapper<>(salaryAccountingItemType)));
    }


    /**
     * 新增核算项目类型
     */
    @PostMapping("/salary/item/type")
    public Result<Boolean> add(@RequestBody AuxiliaryCalculation salaryAccountingItemType)
    {
        salaryAccountingItemTypeService.save(salaryAccountingItemType);
        return success();
    }

    /**
     * 修改核算项目类型
     */
    @PutMapping("/salary/item/type")
    public Result<Boolean> edit(@RequestBody AuxiliaryCalculation salaryAccountingItemType)
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