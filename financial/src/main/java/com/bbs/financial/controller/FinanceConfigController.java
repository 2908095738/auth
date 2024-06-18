package com.bbs.financial.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.entity.FinanceConfig;
import com.bbs.financial.service.FinanceConfigService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static com.bbs.Result.success;

/**
 * 资产配置Controller
 *
 * @author vctgo
 * @date 2024-05-14
 */
@RestController
@RequestMapping("/config")
public class FinanceConfigController {

    @Resource
    private FinanceConfigService financeConfigService;

    /**
     * 查询资产配置列表
     */
//    @RequiresPermissions("system:config:list")
    @GetMapping("/list")
    public Result<Page<FinanceConfig>> list(FinanceConfig financeConfig, @RequestParam Integer current, @RequestParam Integer size) {
        return success(financeConfigService.page(new Page<>(current, size), new QueryWrapper<>(financeConfig)));
    }

    /**
     * 获取资产配置详细信息
     */
//    @RequiresPermissions("system:config:query")
    @GetMapping(value = "/{id}")
    public Result<FinanceConfig> getInfo(@PathVariable("id") Long id) {
        return success(financeConfigService.getById(id));
    }

    /**
     * 新增资产配置
     */
//    @RequiresPermissions("system:config:add")
//    @Log(title = "资产配置", businessType = BusinessType.INSERT)
    @PostMapping
    public Result<Boolean> add(@RequestBody FinanceConfig financeConfig) {
        financeConfigService.save(financeConfig);
        return success();
    }

    /**
     * 修改资产配置
     */
//    @RequiresPermissions("system:config:edit")
//    @Log(title = "资产配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public Result<Boolean> edit(@RequestBody FinanceConfig financeConfig) {
        financeConfigService.updateById(financeConfig);
        return success();
    }

    /**
     * 删除资产配置
     */
//    @RequiresPermissions("system:config:remove")
    //   @Log(title = "资产配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public Result<Boolean> remove(@PathVariable List<Long> ids) {
        financeConfigService.getBaseMapper().deleteBatchIds(ids);
        return success();
    }
}