package com.bbs.financial.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bbs.Result;
import com.bbs.api.auth.UserAPI;
import com.bbs.api.auth.company.CompanyAPI;
import com.bbs.financial.entity.Asset;
import com.bbs.financial.service.AssetService;
import com.bbs.financial.service.CertificateService;
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

    @Resource
    private CertificateService certificateService;


    /**
     * 查询折旧凭证列表
     */
    @GetMapping("/asset/depreciation/debt")
    public Result debt()
    {
        return success(certificateService.selectByDepreciation());
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
