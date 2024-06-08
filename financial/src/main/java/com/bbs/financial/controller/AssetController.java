package com.bbs.financial.controller;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.api.auth.UserAPI;
import com.bbs.api.auth.company.CompanyAPI;
import com.bbs.financial.entity.Asset;
import com.bbs.financial.entity.AssetDepreciationCertificate;
import com.bbs.financial.entity.AssetType;
import com.bbs.financial.entity.Certificate;
import com.bbs.financial.mapper.AssetMapper;
import com.bbs.financial.service.AssetService;
import com.bbs.vo.BaseParam;
import com.bbs.vo.CompanyStructure;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    @Resource
    private AssetMapper assetMapper;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    private static class Param extends BaseParam {

        private Long companyId;

        private String entryMonth;

    }

    /**
     * 查询折旧凭证列表
     */
    @GetMapping("/asset/depreciation/debt")
    public Result<Page<Asset>> debt(Param param){
        Page<Asset> page = assetMapper.selectJoinPage(param.toPage(), Asset.class, new MPJLambdaWrapper<Asset>()
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

    @GetMapping(value = "/asset/{id}")
    public Result<Asset> getInfo(@PathVariable("id") Long id) {
        return success(assetService.getById(id));
    }


    /**
     * 获取资产明细列表
     */
    @GetMapping(value = "/asset/schedule")
    public Result<Page<Asset>> getSchedule(Param param){
        Page<Asset> page = assetMapper.selectJoinPage(param.toPage(), Asset.class, new MPJLambdaWrapper<Asset>()
                .selectAssociation(AssetType.class, Asset::getAssetTypeName, t -> t.result(AssetType::getName))
                .leftJoin(AssetType.class, AssetType::getId, Asset::getAssetTypeId)
                .leftJoin(AssetDepreciationCertificate.class, AssetDepreciationCertificate::getAssetId, Asset::getId)
                .eq(Asset::getIsDeleted, 0)
                .ne(AssetDepreciationCertificate::getAssetId, 0)
                .eq(Asset::getCompanyId, param.getCompanyId())
                .like(nonNull(param.entryMonth), Asset::getUpdateTime, param.entryMonth)
        );

        Set<Long> companyStructureIds = new HashSet<>();
        page.getRecords().forEach(asset -> {
            if(nonNull(asset.getStructureId())) companyStructureIds.add(asset.getStructureId());
        });

        // 查询并填充所属部门
        Map<Long, CompanyStructure> companyStructureIdMap = new HashMap<>();
        if(companyStructureIds.size() > INTEGER_ZERO) {
            companyStructureIdMap = companyAPI
                    .search(companyStructureIds).stream()
                    .collect(Collectors.toMap(CompanyStructure::getId, companyStructure -> companyStructure));
        }

        for (Asset asset : page.getRecords()) {
            if(nonNull(asset.getCompanyId())) {
                //部门
                asset.setStructureName(companyStructureIdMap.getOrDefault(asset.getStructureId(), new CompanyStructure().setName("全部")).getName());
            }
        }
        return success(page);
    }



    /**
     * 获取资产汇总列表
     */
    @GetMapping(value = "/asset/summary")
    public Result<List<Asset>> getSummary(Param param){
        List<Asset> resultSummary = new ArrayList<>();
        List<Asset> list = assetMapper.selectJoinList(Asset.class, new MPJLambdaWrapper<Asset>()
                .selectAssociation(AssetType.class,Asset::getAssetTypeName,t->t.result(AssetType::getName))
                .leftJoin(AssetDepreciationCertificate.class, AssetDepreciationCertificate::getAssetId, Asset::getId)
                .leftJoin(AssetType.class, AssetType::getId, Asset::getAssetTypeId)
                .eq(Asset::getIsDeleted, 0)
                .ne(AssetDepreciationCertificate::getAssetId, 0)
                .eq(Asset::getCompanyId, param.getCompanyId())
                .like(nonNull(param.entryMonth),Asset::getUpdateTime, param.entryMonth)
        );
        if (CollUtil.isNotEmpty(list)){
            //使用stream按类别和部门，合并原值、当月折旧、本年折旧额、期初累计折旧、期末累计折旧、期末减值准备、期末净值
            Map<Long, List<Asset>> collect = list.stream().collect(Collectors.groupingBy(Asset::getAssetTypeId));

            Set<Long> companyStructureIds = new HashSet<>();

            collect.keySet().forEach(assetTypeId -> {
                List<Asset> assets = new ArrayList<>(collect.get(assetTypeId).stream().collect(Collectors.toMap(Asset::getStructureId, a -> a, (o1, o2) -> {
                    o1.setOriginalValue(o1.getOriginalValue() + o2.getOriginalValue());
                    if(nonNull(o2.getDepreciationNowMonthValue())){
                        o1.setAfterPeriod(o2.getDepreciationNowMonthValue());
                    }
                    if (nonNull(o1.getDepreciationNowMonthValue()) && nonNull(o2.getDepreciationNowMonthValue())) {
                        o1.setDepreciationNowMonthValue(o1.getDepreciationNowMonthValue() + o2.getDepreciationNowMonthValue());
                    }
                    if(nonNull(o2.getDepreciationYearValue())){
                        o1.setAfterPeriod(o2.getDepreciationYearValue());
                    }
                    if (nonNull(o1.getDepreciationYearValue()) && nonNull(o2.getDepreciationYearValue())) {
                        o1.setDepreciationYearValue(o1.getDepreciationYearValue() + o2.getDepreciationYearValue());
                    }
                    if(nonNull(o2.getBeginDepreciationAccumulated())){
                        o1.setAfterPeriod(o2.getBeginDepreciationAccumulated());
                    }
                    if (nonNull(o1.getBeginDepreciationAccumulated()) && nonNull(o2.getBeginDepreciationAccumulated())) {
                        o1.setBeginDepreciationAccumulated(o1.getBeginDepreciationAccumulated() + o2.getBeginDepreciationAccumulated());
                    }
                    if(nonNull(o2.getAfterDepreciationAccumulated())){
                        o1.setAfterPeriod(o2.getAfterDepreciationAccumulated());
                    }
                    if (nonNull(o1.getAfterDepreciationAccumulated()) && nonNull(o2.getAfterDepreciationAccumulated())) {
                        o1.setAfterDepreciationAccumulated(o1.getAfterDepreciationAccumulated() + o2.getAfterDepreciationAccumulated());
                    }
                    if(nonNull(o2.getAfterPeriod())){
                        o1.setAfterPeriod(o2.getAfterPeriod());
                    }
                    if (nonNull(o1.getAfterPeriod())&&nonNull(o2.getAfterPeriod())) {
                        o1.setAfterPeriod(o1.getAfterPeriod() + o2.getAfterPeriod());
                    }
                    if(nonNull(o2.getAfterImpairment())){
                        o1.setAfterPeriod(o2.getAfterImpairment());
                    }
                    if (nonNull(o1.getAfterImpairment()) && nonNull(o2.getAfterImpairment())) {
                        o1.setAfterImpairment(o1.getAfterImpairment() + o2.getAfterImpairment());
                    }
                    if(nonNull(o1.getStructureId())) companyStructureIds.add(o1.getStructureId());
                    return o1;
                })).values());
                resultSummary.addAll(assets);
            });

            // 查询并填充所属部门
            Map<Long, CompanyStructure> companyStructureIdMap = new HashMap<>();
            if(companyStructureIds.size() > INTEGER_ZERO) {
                companyStructureIdMap = companyAPI
                        .search(companyStructureIds).stream()
                        .collect(Collectors.toMap(CompanyStructure::getId, companyStructure -> companyStructure));
            }

            for (Asset asset : resultSummary) {
                if(nonNull(asset.getCompanyId())) {
                    //部门
                    asset.setStructureName(companyStructureIdMap.getOrDefault(asset.getStructureId(), new CompanyStructure().setName("全部")).getName());
                }
            }

        }
        return success(resultSummary);
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
