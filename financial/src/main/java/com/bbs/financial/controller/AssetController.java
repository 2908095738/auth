package com.bbs.financial.controller;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bbs.Result;
import com.bbs.api.auth.UserAPI;
import com.bbs.api.auth.company.CompanyAPI;
import com.bbs.financial.entity.Asset;
import com.bbs.financial.entity.Certificate;
import com.bbs.financial.service.AssetService;
import com.bbs.financial.service.CertificateService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Map;

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
     * 折旧凭证
     * 查询资产列表：1当月没有生成折旧凭证 2开始使用日期月份比当前月份小
     * 计算要生成的值：
     */
    @GetMapping("/asset/depreciation/debt")
    public Result debt()
    {
        //当月有没有生成折旧凭证
        Certificate certificate = certificateService.selectByNowDepreciation();
        if(Objects.nonNull(certificate)){
            return success(certificate);
        }{
            List<Asset> assets = assetService.selectNowJoinList();
            Long result = 0L;
            if(CollUtil.isNotEmpty(assets)){
                for (Asset asset : assets) {
                    Integer depreciationMethod = asset.getDepreciationMethod();//折旧方法
                    if(depreciationMethod == 1){
                        //平均年限法
                        Long depreciationMonthValue = asset.getDepreciationMonthValue();//平均月折旧额
                        result= result + depreciationMonthValue;
                    }else if(depreciationMethod == 2){
                        //双倍余额递减法
                        Integer durableMonths = asset.getDurableMonths();//预计使用月数
                        Date startDate = asset.getStartDate();//开始时间：2023-05-01
                        Date nowDate = new Date();//现在时间：2024-05-31

                        Long originalValue = asset.getOriginalValue();//原值
                        int durableYears = durableMonths / 12;//预计使用年限
                        Long ratioRemainingValue = asset.getRatioRemainingValue();//预计残值


                        //TODO 把

                    }
                }
            }
        Map<String, Long> map = new HashMap<>();
        map.put("num",result);
        return success(map);
        }
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
