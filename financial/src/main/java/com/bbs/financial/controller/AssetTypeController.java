package com.bbs.financial.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.entity.AssetType;
import com.bbs.financial.service.AssetTypeService;
import com.bbs.financial.util.LoginUser;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static com.bbs.Result.success;

/**
 * 资产类别Controller
 * @author vctgo
 * @date 2024-05-28
 */
@RestController
public class AssetTypeController {

    @Resource
    private AssetTypeService assetTypeService;

    /**
     * 查询资产类别列表
     */
    @GetMapping("/asset/type/list")
    public Result<Page<AssetType>> list(AssetType assetType, @RequestParam Integer current, @RequestParam Integer size)
    {
        return success(assetTypeService.page(new Page<>(current, size), new QueryWrapper<>(assetType)));
    }

    /**
     * 获取资产类别详细信息
     */
    @GetMapping(value = "/asset/type/{id}")
    public Result<AssetType> getInfo(@PathVariable("id") Long id)
    {
        return success(assetTypeService.getById(id));
    }

    /**
     * 新增资产类别
     */
    @PostMapping("/asset/type")
    public Result<Boolean> add(@RequestBody AssetType assetType)
    {
        assetType.setCreateBy(LoginUser.getId());
        assetType.setUpdateBy(LoginUser.getId());
        assetTypeService.save(assetType);
        return success();
    }

    /**
     * 修改资产类别
     */
    @PutMapping("/asset/type")
    public Result<Boolean> edit(@RequestBody AssetType assetType)
    {
        assetTypeService.updateById(assetType);
        return success();
    }

    /**
     * 删除资产类别
     */
    @DeleteMapping("/asset/type/{ids}")
    public Result<Boolean> remove(@PathVariable List<Long> ids)
    {
        assetTypeService.getBaseMapper().deleteBatchIds(ids);
        return success();
    }
}
