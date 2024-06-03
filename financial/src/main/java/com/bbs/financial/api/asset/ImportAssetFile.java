package com.bbs.financial.api.asset;

import cn.hutool.core.date.DateUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.bbs.Result;
import com.bbs.api.auth.User;
import com.bbs.api.auth.UserAPI;
import com.bbs.api.auth.company.CompanyAPI;
import com.bbs.financial.entity.Asset;
import com.bbs.financial.entity.AssetImportRecord;
import com.bbs.financial.entity.AssetNumUnit;
import com.bbs.financial.entity.AssetType;
import com.bbs.financial.service.AssetImportRecordService;
import com.bbs.financial.service.AssetNumUnitService;
import com.bbs.financial.service.AssetService;
import com.bbs.financial.service.AssetTypeService;
import com.bbs.financial.util.LoginUser;
import com.bbs.vo.CompanyStructure;
import com.google.common.base.Preconditions;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.math.NumberUtils.*;

@RestController
@RequestMapping
public class ImportAssetFile {

    public static Map<String, String> DEFAULT_TEMPLATE_HEADER_ALIAS = new HashMap<String, String>() {{
        put("资产编码", "no");
        put("资产名称", "name");
        put("资产类别", "assetTypeName");//临时字段，需要修改到其他字段
        put("部门", "structureName");//临时字段，需要修改到其他字段
        put("使用人", "useUserName");//临时字段，需要修改到其他字段
        put("开始使用日期", "startDate");
        put("数量", "num");
        put("数量单位", "numUnitName");//临时字段，需要修改到其他字段
        put("规格型号", "spec");
        put("存放地点", "storagePlace");
        put("折旧方法", "depreciationMethod");
        put("使用月数", "durableMonths");
        put("原值", "originalValue");
        put("税额", "amountTaxPaid");
        put("残值率", "ratioRemaining");
        put("预计残值", "ratioRemainingValue");
        put("减值准备", "depreciationYearValue");
        put("已折旧月数", "depreciationMonths");
        put("期初净值", "beginDepreciationAccumulated");
        put("期初累计折旧", "beginPeriod");
        put("平均月折旧额", "depreciationMonthValue");
        put("当月折旧额", "depreciationMonthValue");
        put("本年折旧额", "depreciationYearValue");
        put("期末累计折旧", "endPeriod");
        put("期末净值", "endNetValue");
        put("期末减值准备", "endDepreciationYearValue");
        put("清理月份", "cleanMonth");
        put("状态", "status");
        put("备注", "remark");
        put("创建时间", "createTime");
        put("信息创建人", "createUserName"); //临时字段，需要修改到其他字段
        put("修改时间", "updateTime");
        put("信息修改人", "updateUserName"); //临时字段，需要修改到其他字段
    }};

    @Resource
    private AssetService assetService;
    @Resource
    private AssetTypeService assetTypeService;
    @Resource
    private AssetNumUnitService assetNumUnitService;
    @DubboReference
    private CompanyAPI companyAPI;
    @DubboReference
    private UserAPI userAPI;
    @Resource
    private AssetImportRecordService recordService;
    @Resource
    private TransactionDefinition transactionDefinition;
    @Resource
    private DataSourceTransactionManager transactionManager;

    private static final String PARSE_ERROR_MSG = "文件解析失败，请检查内容是否与模板一致";

    @PostMapping("/asset/import/template")
    public Result<Boolean> importFile(@RequestParam Long companyId, @RequestParam MultipartFile file) throws IllegalArgumentException {

        // 创建导入记录
        AssetImportRecord record = new AssetImportRecord(companyId, LoginUser.getId());

        // 解析 excel
        List<Asset> assets = parseExcelToAssetList(file);

        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            // 填充各种字段（例如创建人，通过创建人名称查询用户信息，将 UID 回填）
            fillProperty(assets, companyId);

            // 批量入库
            boolean addResult = assetService.saveBatch(assets);

            // 根据入库结果，决定是否 commit 或 rollback，并 save 导入记录
            if(addResult) {
                setSuccessImportResultAndCommit(record, assets.size(), transaction);
            } else {
                setFailedImportResultAndRollback(record, transaction);
            }
            // save 导入记录
            recordImportResult(record);
            return Result.success();
        } catch (Exception error) {
            // 设置导入记录 & rollback
            setErrorImportResultAndRollback(record, error, transaction);
            // save 导入记录
            recordImportResult(record);
            return Result.failed();
        }
    }

    private void setSuccessImportResultAndCommit(AssetImportRecord record, Integer importSize, TransactionStatus transaction) {
        record.setResult(INTEGER_ZERO);
        record.setSize(importSize);
        transactionManager.commit(transaction);
    }

    private void setFailedImportResultAndRollback(AssetImportRecord record, TransactionStatus transaction) {
        record.setResult(INTEGER_ONE);
        transactionManager.rollback(transaction);
    }

    private void setErrorImportResultAndRollback(AssetImportRecord record, Exception error, TransactionStatus transaction) {
        transactionManager.rollback(transaction);
        error.printStackTrace();
        record.setResult(INTEGER_ONE);
        record.setErrorMessage(error.getMessage());
    }

    private void recordImportResult(AssetImportRecord record) {
        recordService.save(record);
    }

    private List<Asset> parseExcelToAssetList(MultipartFile file) throws IllegalArgumentException {
        List<Asset> assets;
        try {
            ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
            reader.setHeaderAlias(DEFAULT_TEMPLATE_HEADER_ALIAS);
            assets = reader.readAll(Asset.class);
        } catch (IOException e) {
            throw new IllegalArgumentException(PARSE_ERROR_MSG);
        }
        Preconditions.checkArgument(nonNull(assets), PARSE_ERROR_MSG);
        return assets;
    }

    private void fillProperty(List<Asset> assets, Long companyId) {
        assets.forEach(asset -> {
            fillAssetType(asset);
            // 如果未设置编码，则使用【公司ID + 日期 + 已有资产数量（去重）】当作默认编码
            fillNo(asset, companyId);
            fillAssetTypeName(asset);
            fillStructure(asset, companyId);
            fillUseUser(asset);
            fillCreateUser(asset);
            fillUpdateUser(asset);
            fillNumUnit(asset, companyId);
        });
    }


    private void fillAssetType(Asset asset) {
        if(isNotBlank(asset.getNo())) {
            AssetType assetType = assetTypeService.lambdaQuery().eq(AssetType::getName, asset.getAssetTypeName()).one();
            if(nonNull(assetType)) asset.setAssetTypeId(assetType.getId());
        }
    }
    private void fillNo(Asset asset, Long companyId) {
        if(isBlank(asset.getNo())) {
            Long count = assetService.lambdaQuery().eq(Asset::getCompanyId, companyId).count();
            asset.setNo(
                    companyId +
                            DateUtil.format(new Date(), "yyyyMMdd") +
                            (Objects.equals(count, LONG_ZERO) ? LONG_ONE : count)
            );
        }
    }

    private void fillAssetTypeName(Asset asset) {
        if(isNotBlank(asset.getAssetTypeName())) {
            AssetType assetType = assetTypeService.lambdaQuery().eq(AssetType::getName, asset.getAssetTypeName()).one();
            if(nonNull(assetType)) asset.setAssetTypeId(assetType.getId());
        }
    }

    private void fillStructure(Asset asset, Long companyId) {
        if(isNotBlank(asset.getStructureName())) {
            CompanyStructure structure = companyAPI.searchStructureNames(companyId, asset.getStructureName());
            if(nonNull(structure)) asset.setStructureId(structure.getId());
        }
    }
    private void fillUseUser(Asset asset) {
        if(isNotBlank(asset.getUseUserName())) {
            User user = userAPI.getUserByName(asset.getUseUserName());
            if(nonNull(user)) asset.setUseUserId(user.getId());
        }
    }

    private void fillCreateUser(Asset asset) {
        if(isNotBlank(asset.getCreateUserName())) {
            User user = userAPI.getUserByName(asset.getCreateUserName());
            if(nonNull(user)) asset.setCreateBy(user.getId());
        }
    }

    private void fillUpdateUser(Asset asset) {
        if(isNotBlank(asset.getUpdateUserName())) {
            User user = userAPI.getUserByName(asset.getUpdateUserName());
            if(nonNull(user)) asset.setUseUserId(user.getId());
        }
    }

    private void fillNumUnit(Asset asset, Long companyId) {
        if(isNotBlank(asset.getNumUnitName())) {
            AssetNumUnit unit = assetNumUnitService.lambdaQuery()
                    .and(wrapper -> wrapper
                            .eq(AssetNumUnit::getCompanyId, INTEGER_ZERO)
                            .or()
                            .eq(AssetNumUnit::getCompanyId, companyId)
                    )
                    .eq(AssetNumUnit::getName, asset.getNumUnitName())
                    .one();
            if(nonNull(unit)) asset.setNumUnitId(unit.getId());
        }
    }
}
