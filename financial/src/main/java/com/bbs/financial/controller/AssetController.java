package com.bbs.financial.controller;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.entity.Asset;
import com.bbs.financial.entity.Certificate;
import com.bbs.financial.service.AssetService;
import com.bbs.financial.service.CertificateService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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

    @Resource
    private CertificateService certificateService;

    /**
     * 查询资产列表
     */
    @GetMapping("/asset/list")
    public Result<Page<Asset>> list(Asset asset, @RequestParam Integer current, @RequestParam Integer size)
    {
        return success(assetService.page(new Page<>(current, size), new QueryWrapper<>(asset)));
    }

    /**
     * 折旧凭证
     * 查询资产列表：1当月没有生成折旧凭证 2开始使用日期月份比当前月份小
     * 计算要生成的值：
     */
    @GetMapping("/asset/depreciation/debt")
    public Result debt()
    {
        Certificate certificate = certificateService.selectByNowDepreciation();
        if(Objects.nonNull(certificate)){
            return success(certificate);
        }{
            List<Asset> assets = assetService.selectNowJoinList();
            Long result = 0L;
            if(CollUtil.isNotEmpty(assets)){
                for (Asset asset : assets) {
                    Date startDate = asset.getStartDate();//开始时间
                    Integer depreciationMethod = asset.getDepreciationMethod();//折旧方法
                    if(depreciationMethod == 1){
                        //平均年限法
                        Long depreciationMonthValue = asset.getDepreciationMonthValue();//平均月折旧额
                        result= result + depreciationMonthValue;
                    }else if(depreciationMethod == 2){
                        //双倍余额递减法
                        Long originalValue = asset.getOriginalValue();//原值

                    }



                }
            }
            return success(result);
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
}
