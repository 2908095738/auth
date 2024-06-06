package com.bbs.financial.api.asset;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.api.auth.User;
import com.bbs.api.auth.UserAPI;
import com.bbs.api.auth.company.CompanyAPI;
import com.bbs.financial.converter.AssetConverter;
import com.bbs.financial.entity.Asset;
import com.bbs.financial.entity.AssetNumUnit;
import com.bbs.financial.entity.AssetType;
import com.bbs.financial.mapper.AssetMapper;
import com.bbs.vo.CompanyStructure;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.bbs.Result.success;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;

@RestController
@RequestMapping
public class SearchAssetList {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {

        /**
         *
         */
        private Long id;

        /**
         * 公司主键
         */
        private Long companyId;

        /**
         * 资产编码
         */
        private String no;

        /**
         * 资产名称
         */
        private String name;

        /**
         * 资产类别
         */
        private Long assetTypeId;

        /**
         * 部门ID（公司结构ID）
         */
        private Long structureId;

        /**
         * 开始使用日期
         */
        private Date startDate;

        /**
         * 数量
         */
        private Long num;

        /**
         * 数量单位
         */
        private Long numUnitId;

        /**
         * 规格型号
         */
        private String spec;

        /**
         * 存放地点
         */
        private String storagePlace;

        /**
         * 使用人id
         */
        private Long useUserId;

        /**
         * 折旧方法
         */
        private Integer depreciationMethod;

        /**
         * 使用月数
         */
        private Integer durableMonths;

        /**
         * 原值
         */
        private Long originalValue;

        /**
         * 税额
         */
        private Long amountTaxPaid;

        /**
         * 残值率
         */
        private Long ratioRemaining;

        /**
         * 预计残值
         */
        private Long ratioRemainingValue;

        /**
         * 减值准备
         */
        private Long impairment;

        /**
         * 已折旧月数
         */
        private Integer depreciationMonths;

        /**
         * 期初累计折旧
         */
        private Long beginDepreciationAccumulated;

        /**
         * 期初净值=原值-期初累计折旧
         */
        private Long beginPeriod;

        /**
         * 月折旧额
         */
        private Long depreciationMonthValue;

        /**
         * 清理月份
         */
        private Long assetsCleanTime;

        /**
         * 状态:正常 清理
         */
        private Integer status;

        /**
         * 备注
         */
        private String remark;

        /**
         * 创建时间
         */
        private Date createTime;


        /**
         * 创建时间
         */
        private Long createTimeLong;

        /**
         * 信息创建人
         */
        private Long createBy;

        /**
         * 修改时间
         */
        private Date updateTime;

        /**
         * 信息修改人
         */
        private Long updateBy;

        private Integer current;

        private Integer size;
    }

    @Resource
    private AssetConverter converter;
    @DubboReference
    private CompanyAPI companyAPI;
    @Resource
    private AssetMapper assetMapper;

    @DubboReference
    private UserAPI userAPI;

    @GetMapping("/asset/list")
    public Result<Page<Asset>> list(Param param) {
        Asset entity = converter.toEntity(param);

        Date createTime = converter(param.getCreateTimeLong());
        Date assetsCleanTime = converter(param.getAssetsCleanTime());
        Page<Asset> result = assetMapper.selectJoinPage(new Page<>(param.getCurrent(), param.getSize()), Asset.class, new MPJLambdaWrapper<>(entity)
                .selectAll(Asset.class)
                .leftJoin(AssetNumUnit.class, AssetNumUnit::getId, Asset::getNumUnitId, ext -> ext
                        .selectAssociation(AssetNumUnit.class, Asset::getNumUnit)
                )
                .leftJoin(AssetType.class, AssetType::getId, Asset::getAssetTypeId, ext -> ext
                        .selectAssociation(AssetType.class, Asset::getAssetType)
                )
                .eq(Asset::getStatus, INTEGER_ZERO)
                .and(nonNull(createTime), wrapper -> wrapper
                        .ge(Asset::getCreateTime, nonNull(createTime) ? DateUtil.beginOfMonth(createTime) : null)
                        .lt(Asset::getCreateTime, nonNull(createTime) ? DateUtil.offsetMonth(createTime, INTEGER_ONE) : null)
                )
                .and(nonNull(assetsCleanTime), wrapper -> wrapper
                        .ge(Asset::getAssetsCleanTime, nonNull(assetsCleanTime) ? DateUtil.beginOfMonth(assetsCleanTime) : null)
                        .lt(Asset::getAssetsCleanTime, nonNull(assetsCleanTime) ? DateUtil.offsetMonth(assetsCleanTime, INTEGER_ONE) : null)
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

    private Date converter(Long dateLong) {
        Date time = null;
        if(nonNull(dateLong)) {
            time = new Date(dateLong);
        }
        return time;
    }
}

