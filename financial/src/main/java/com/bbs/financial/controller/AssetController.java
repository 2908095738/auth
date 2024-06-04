package com.bbs.financial.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.api.auth.UserAPI;
import com.bbs.api.auth.company.CompanyAPI;
import com.bbs.financial.entity.Asset;
import com.bbs.financial.entity.AssetDepreciationCertificate;
import com.bbs.financial.entity.AssetType;
import com.bbs.financial.entity.Certificate;
import com.bbs.financial.service.AssetService;
import com.bbs.vo.BaseParam;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

import static com.bbs.Result.success;

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


    @Data
    private static class Param extends BaseParam {

        private Long companyId;

    }

    /**
     * 查询折旧凭证列表
     */
    @GetMapping("/asset/depreciation/debt")
    public Result<Page<Asset>> debt(Param param){
        Page<Asset> page = assetService.selectJoinListPage(param.toPage(),Asset.class, new MPJLambdaWrapper<Asset>()
                .selectAll(Asset.class)
                .selectAssociation(AssetType.class,Asset::getAssetTypeName,t->t.result(AssetType::getName))
                .leftJoin(AssetType.class, AssetType::getId, Asset::getAssetTypeId)
                .selectCollection(AssetDepreciationCertificate.class,Asset::getCertificatesDepreciationList, o->
                        o.association(Certificate.class, AssetDepreciationCertificate::getCertificate))
                .leftJoin(AssetDepreciationCertificate.class, AssetDepreciationCertificate::getAssetId, Asset::getId)
                .leftJoin(Certificate.class,Certificate::getId, AssetDepreciationCertificate::getDepreciationCertificateId)
                .eq(Asset::getIsDeleted, 0)
                .ne(Asset::getDepreciationMethod, 3)//排除折旧方法为不计提折旧的资产
                .ne(AssetDepreciationCertificate::getAssetId, 0)//排除未生成折旧凭证的资产
        );
        return success(page);
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
