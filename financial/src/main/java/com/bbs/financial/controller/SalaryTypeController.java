package com.bbs.financial.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.entity.SalaryType;
import com.bbs.financial.service.SalaryTypeService;
import lombok.Data;
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
 * 工资类型Controller
 * @author vctgo
 * @date 2024-05-15
 */
@RestController
public class SalaryTypeController {

    @Resource
    private SalaryTypeService salaryTypeService;

    /**
     * 查询工资类型列表
     */
    @GetMapping("/type/list")
    public Result<Page<SalaryType>> list(SalaryType salaryType, @RequestParam Integer current, @RequestParam Integer size)
    {
        return success(salaryTypeService.page(new Page<>(current, size), new QueryWrapper<>(salaryType)));
    }



    @Data
    private static class ListParam extends SalaryType {

    }

    /**
     * 新增工资类型
     */
    @PostMapping("/type")
    public Result<Boolean> add(@RequestBody ListParam param)
    {
        salaryTypeService.save(new SalaryType().setTypeName(param.getTypeName()).setCompanyId(param.getCompanyId()));
        return success();
    }

    /**
     * 修改工资类型
     */
    @PutMapping("/type")
    public Result<Boolean> edit(@RequestBody SalaryType salaryType)
    {
        salaryTypeService.updateById(salaryType);
        return success();
    }

    /**
     * 删除工资类型
     */
    @DeleteMapping("/type/{ids}")
    public Result<Boolean> remove(@PathVariable List<Long> ids)
    {
        salaryTypeService.getBaseMapper().deleteBatchIds(ids);
        return success();
    }
}