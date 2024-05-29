package com.bbs.financial.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.entity.AssetChangeLog;
import com.bbs.financial.service.AssetChangeLogService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static com.bbs.Result.success;

/**
 * 资产变动Controller
 * @author vctgo
 * @date 2024-05-29
 */
@RestController
public class AssetChangeLogController {

    @Resource
    private AssetChangeLogService assetChangeLogService;

    /**
     * 查询资产变动列表
     */
    @GetMapping("/change/list")
    public Result<Page<AssetChangeLog>> list(AssetChangeLog assetChangeLog, @RequestParam Integer current, @RequestParam Integer size)
    {
        return success(assetChangeLogService.page(new Page<>(current, size), new QueryWrapper<>(assetChangeLog)));
    }

    /**
     * 获取资产变动详细信息
     */
    @GetMapping(value = "/change/{id}")
    public Result<AssetChangeLog> getInfo(@PathVariable("id") Long id)
    {
        return success(assetChangeLogService.getById(id));
    }

    /**
     * 新增资产变动
     */
    @PostMapping("/change")
    public Result<Boolean> add(@RequestBody AssetChangeLog assetChangeLog)
    {
        assetChangeLogService.save(assetChangeLog);
        return success();
    }

    /**
     * 修改资产变动
     */
    @PutMapping("/change")
    public Result<Boolean> edit(@RequestBody AssetChangeLog assetChangeLog)
    {
        assetChangeLogService.updateById(assetChangeLog);
        return success();
    }

    /**
     * 删除资产变动
     */
    @DeleteMapping("/change/{ids}")
    public Result<Boolean> remove(@PathVariable List<Long> ids)
    {
        assetChangeLogService.getBaseMapper().deleteBatchIds(ids);
        return success();
    }
}
