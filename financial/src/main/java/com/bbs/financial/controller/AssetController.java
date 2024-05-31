package com.bbs.financial.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.api.auth.User;
import com.bbs.api.auth.UserAPI;
import com.bbs.api.auth.company.CompanyAPI;
import com.bbs.financial.entity.Asset;
import com.bbs.financial.entity.AssetNumUnit;
import com.bbs.financial.entity.AssetType;
import com.bbs.financial.service.AssetService;
import com.bbs.vo.CompanyStructure;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.bbs.Result.success;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;

/**
 * 资产Controller
 * @author vctgo
 * @date 2024-05-28
 */
@RestController
public class AssetController {

    @Resource
    private AssetService assetService;
    @DubboReference
    private CompanyAPI companyAPI;

    @DubboReference
    private UserAPI userAPI;

    /**
     * 查询资产列表
     */
    @GetMapping("/asset/list")
    public Result<Page<Asset>> list(Asset assetParam, @RequestParam Integer current, @RequestParam Integer size)
    {
        Page<Asset> result = assetService.selectJoinListPage(new Page<>(current, size), Asset.class, new MPJLambdaWrapper<>(assetParam)
                .selectAll(Asset.class)
                .leftJoin(AssetNumUnit.class, AssetNumUnit::getId, Asset::getNumUnitId, ext -> ext
                        .selectAssociation(AssetNumUnit.class, Asset::getNumUnit)
                )
                .leftJoin(AssetType.class, AssetType::getId, Asset::getAssetTypeId, ext -> ext
                        .selectAssociation(AssetType.class, Asset::getAssetType)
                )
        );
        Set<Long> companyStructureIds = new HashSet<>();
        Set<Long> userIds = new HashSet<>();
        result.getRecords().forEach(asset -> {
            userIds.add(asset.getCreateBy());
            if(nonNull(asset.getStructureId())) companyStructureIds.add(asset.getStructureId());
            if(nonNull(asset.getUseUserId())) userIds.add(asset.getUseUserId());
            if(nonNull(asset.getUpdateBy())) userIds.add(asset.getUpdateBy());
        });
        // 查询并填充所属部门
        Map<Long, CompanyStructure> companyStructureIdMap = null;
        if(companyStructureIds.size() > INTEGER_ZERO) {
            companyStructureIdMap = companyAPI
                    .search(companyStructureIds).stream()
                    .collect(Collectors.toMap(CompanyStructure::getId, companyStructure -> companyStructure));
        }
        // 查询并填充所属用户、创建用户、修改用户
        Map<Long, User> userIdMap = null;
        if(userIds.size() > INTEGER_ZERO) {
            userIdMap = userAPI.getUserList(userIds)
                    .stream().collect(Collectors.toMap(User::getId, user -> user));
        }
        boolean companyStructureIdMapIsNull = nonNull(companyStructureIdMap);
        boolean userIdMapIsNull = nonNull(userIdMap);
        if(companyStructureIdMapIsNull || userIdMapIsNull) {
            for (Asset asset : result.getRecords()) {
                if(companyStructureIdMapIsNull && nonNull(asset.getCompanyId())) {
                    asset.setCompanyStructure(companyStructureIdMap.get(asset.getCompanyId()));
                }
                if(userIdMapIsNull) {
                    asset.setCreateUser(userIdMap.get(asset.getCreateBy()));
                    if(nonNull(asset.getUseUserId())) asset.setUseUser(userIdMap.get(asset.getUseUserId()));
                    if(nonNull(asset.getUpdateBy())) asset.setUpdateUser(userIdMap.get(asset.getUpdateBy()));
                }
            }
        }
        return success(result);
    }

    /**
     * 折旧凭证
     * 查询资产列表：1当月没有生成折旧凭证 2开始使用日期月份比当前月份小
     * 计算要生成的值：
     */
    @GetMapping("/asset/depreciation/debt")
    public Result<Boolean> debt()
    {
        return success();
    }


    /**
     * 获取资产详细信息
     */
    @GetMapping(value = "/asset/{id}")
    public Result<Asset> getInfo(@PathVariable("id") Long id)
    {
        return success(assetService.getById(id));
    }


    /**
     * 修改资产
     */
    @PostMapping("/asset")
    public Result<Boolean> edit(@RequestBody Asset asset)
    {
        assetService.updateById(asset);
        return success();
    }

    /**
     * 删除资产
     */
    @DeleteMapping("/asset/{ids}")
    public Result<Boolean> remove(@PathVariable List<Long> ids)
    {
        assetService.getBaseMapper().deleteBatchIds(ids);
        return success();
    }


    /**
     * 查询存放地点
     */
    @GetMapping("/asset/storage/place")
    public Result<List<String>> searchStoragePlace(
            @RequestParam Long companyId,
            @RequestParam(required = false) String name
    ) {
        return success(assetService.listObjs(new QueryWrapper<Asset>()
                .select("DISTINCT storage_place")
                .eq("company_id", companyId)
                .like(StringUtils.isNotBlank(name), "storage_place", name)
                .isNotNull("storage_place")
                .orderByAsc("storage_place")
        ));
    }
}
