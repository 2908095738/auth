package com.bbs.financial.api.asset;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ReflectUtil;
import com.bbs.Result;
import com.bbs.api.auth.company.CompanyAPI;
import com.bbs.financial.entity.Asset;
import com.bbs.financial.entity.AssetChangeLog;
import com.bbs.financial.entity.AssetType;
import com.bbs.financial.enums.ChangeItemEnum;
import com.bbs.financial.enums.DepreciationMethod;
import com.bbs.financial.service.AssetChangeLogService;
import com.bbs.financial.service.AssetService;
import com.bbs.financial.service.AssetTypeService;
import com.bbs.financial.util.LoginUser;
import com.bbs.vo.CompanyStructure;
import org.apache.commons.compress.utils.Sets;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.bbs.Result.success;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.math.NumberUtils.LONG_ONE;
import static org.apache.commons.lang3.math.NumberUtils.LONG_ZERO;


@RestController
public class AddAsset {

    @DubboReference
    private CompanyAPI companyAPI;
    @Resource
    private AssetService assetService;
    @Resource
    private AssetChangeLogService assetChangeLogService;
    @Resource
    private AssetTypeService assetTypeService;
    @Resource
    private TransactionDefinition transactionDefinition;
    @Resource
    private DataSourceTransactionManager transactionManager;


    /**
     * 新增资产
     */
    @PutMapping("/asset")
    public Result<Long> add(@Valid @RequestBody Asset asset) {

        boolean isCreate = isCreate(asset);

        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            if(isCreate) {
                tryFillNo(asset);
                fillCreateUser(asset);
            } else {
                Asset dbAsset = assetService.getById(asset.getId());

                List<AssetChangeLog> assetChangeLogs = new ArrayList<>();

                Field[] fields = ReflectUtil.getFields(Asset.class);// 获取所有字段
                for (Field field : fields) {
                    field.setAccessible(true);
                    String name = ChangeItemEnum.getName(field.getName());
                    Object beforValue = field.get(dbAsset);
                    Object value = field.get(asset);

                    // 如果有值，并且值跟旧值不一样，则更新
                    if (nonNull(value) && nonNull(beforValue) &&!value.equals(beforValue)&& nonNull(name)) {
                        // 原值、减值准备、残值率、期初累计折旧
                        if(ChangeItemEnum.ASSET_VALUE.getName().equals(name)
                                ||ChangeItemEnum.DEPRECIATION_PREPARATION.getName().equals(name)
                                ||ChangeItemEnum.RESIDUAL_VALUE_RATE.getName().equals(name)
                                ||ChangeItemEnum.ACCUMULATED_DEPRECIATION.getName().equals(name)) {
                            beforValue = Long.parseLong(beforValue.toString())/100;
                            value = Long.parseLong(value.toString())/100;
                        }
                        //折旧方法
                        if(ChangeItemEnum.DEPRECIATION_METHOD.getName().equals(name)) {
                            beforValue = DepreciationMethod.getValueByKey((int) beforValue);
                            value =  DepreciationMethod.getValueByKey((int) value);
                        }
                        //部门
                        if(ChangeItemEnum.USE_DEPARTMENT.getName().equals(name)) {
                            Map<Long, CompanyStructure> structureMap = companyAPI.searchIdMap(Sets.newHashSet(Long.parseLong(String.valueOf(beforValue)),Long.parseLong(String.valueOf(value))));
                            beforValue = structureMap.get((long) beforValue).getName();
                            value = structureMap.get((long) value).getName();
                        }
                        //资产类别
                        if(ChangeItemEnum.ASSET_CATEGORY.getName().equals(name)) {
                            Map<Long, AssetType> assetTypeMap = assetTypeService.listByIds(Sets.newHashSet(Long.parseLong(String.valueOf(beforValue)), Long.parseLong(String.valueOf(value)))).stream().collect(Collectors.toMap(AssetType::getId, structure -> structure));;
                            beforValue = assetTypeMap.get((long) beforValue).getName();
                            value = assetTypeMap.get((long) value).getName();
                        }
                        assetChangeLogs.add(new AssetChangeLog(dbAsset.getCompanyId(), dbAsset.getNo(), dbAsset.getName(),
                                "变动 "+name,String.valueOf(beforValue), String.valueOf(value),LoginUser.getId()));
                    }
                }
                assetChangeLogService.saveBatch(assetChangeLogs);
                fillUpdateUser(asset);
            }

            assetService.saveOrUpdate(asset);

            transactionManager.commit(transaction);
            return success(asset.getId());
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new RuntimeException(e);
        }
    }

    private boolean isCreate(Asset asset) {
        return isNull(asset.getId());
    }

    private boolean isSetAssetNo(Asset asset) {
        return isBlank(asset.getNo());
    }

    private void fillCreateUser(Asset asset) {
        asset.setCreateBy(LoginUser.getId());
    }

    private void generateAndFillNo(Asset asset) {
        // 如果未设置编码，则使用【公司ID + 日期 + 已有资产数量（去重）】当作默认编码
        Long count = assetService.lambdaQuery().eq(Asset::getCompanyId, asset.getCompanyId()).count();
        asset.setNo(
                asset.getCompanyId() +
                        DateUtil.format(new Date(), "yyyyMMdd") +
                        (Objects.equals(count, LONG_ZERO) ? LONG_ONE : count+LONG_ONE)
        );
    }

    private void tryFillNo(Asset asset) {
        if(isSetAssetNo(asset)) generateAndFillNo(asset);
    }

    private void fillUpdateUser(Asset asset) {
        asset.setUpdateBy(LoginUser.getId());
    }
}
