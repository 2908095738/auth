package com.bbs.financial.api.asset;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bbs.Result;
import com.bbs.financial.converter.AssetConverter;
import com.bbs.financial.entity.Asset;
import com.bbs.financial.entity.AssetAccountCertificate;
import com.bbs.financial.entity.AssetNumUnit;
import com.bbs.financial.service.AssetAccountCertificateService;
import com.bbs.financial.service.AssetNumUnitService;
import com.bbs.financial.service.AssetService;
import com.bbs.financial.util.LoginUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.bbs.Result.success;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.math.NumberUtils.LONG_ONE;
import static org.apache.commons.lang3.math.NumberUtils.LONG_ZERO;


@RestController
public class AddAsset {

    @Resource
    private AssetService assetService;
    @Resource
    private AssetConverter converter;
    @Resource
    private AssetNumUnitService numUnitService;
    @Resource
    private TransactionDefinition transactionDefinition;
    @Resource
    private DataSourceTransactionManager transactionManager;
    @Resource
    private AssetAccountCertificateService assetAccountCertificateService;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Param {
        /**
         * 主键
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
         * ps: 如果选择已存在的，则为 ID，新增则为 String
         */
        private String numUnit;

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
         * 状态:正常 清理
         */
        private Integer status;

        /**
         * 备注
         */
        private String remark;

        /**
         * 固定资产科目
         */
        private Long fixedAssetsAccountId;

        /**
         * 资产购入对方科目
         */
        private Long purchaseAssetsOtherPartAccountId;

        /**
         * 资产凭证id
         */
        private Long assetsCertificateId;

        /**
         * 税金科目
         */
        private Long taxesAccountId;

        /**
         * 折旧科目
         */
        private Long depreciationAccountId;

        /**
         * 折旧费用科目
         */
        private Long depreciationCostAccountId;

        /**
         * 当月折旧凭证id
         */
        private Long depreciationCertificateId;

        /**
         * 资产清理科目
         */
        private Long assetsCleanAccountId;

        /**
         * 资产清理凭证id
         */
        private Long assetsCleanCertificateId;

        /**
         * 清理月份
         */
        private String assetsCleanMonth;

        /**
         * 减值准备科目
         */
        private Long impairmentAccountId;

        /**
         * 减值准备对方科目
         */
        private Long impairmentOtherPartAccountId;

        /**
         * 减值凭证id
         */
        private Long impairmentCertificateId;

        /**
         * 其他凭证id
         */
        private Long otherCertificateId;
    }

    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");

    public static boolean isNumeric(String str) {
        return str != null && NUMBER_PATTERN.matcher(str).matches();
    }


    /**
     * 新增资产
     */
    @PutMapping("/asset")
    public Result<Long> add(@RequestBody Param param) {
        Asset asset = converter.toEntity(param);
        AssetAccountCertificate assetAccountCertificate = converter.toAccountCertificateEntity(param);

        boolean isCreate = isCreate(asset);

        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            // 填充数量单位，如果不存在则先创建（取决于值的类型）
            fillOrCreateNumberUnit(param, asset);

            if(isCreate) {
                tryFillNo(asset);
                fillCreateUser(asset);
            } else {
                fillUpdateUser(asset);
            }

            assetService.saveOrUpdate(asset);

            if(isCreate) {
                assetAccountCertificate.setAssetId(asset.getId());
                assetAccountCertificateService.save(assetAccountCertificate);
            } else {
                assetAccountCertificateService.update(assetAccountCertificate, new LambdaQueryWrapper<AssetAccountCertificate>()
                        .eq(AssetAccountCertificate::getAssetId, asset.getId())
                );
            }
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
                        (Objects.equals(count, LONG_ZERO) ? LONG_ONE : count)
        );
    }

    private void tryFillNo(Asset asset) {
        if(isSetAssetNo(asset)) generateAndFillNo(asset);
    }

    private void fillUpdateUser(Asset asset) {
        asset.setUpdateBy(LoginUser.getId());
    }



    private void fillOrCreateNumberUnit(Param param, Asset asset) {
        String numUnit = param.getNumUnit();
        if(isNumeric(numUnit)) {
            asset.setNumUnitId(Long.valueOf(numUnit));
        } else {
            AssetNumUnit unit = new AssetNumUnit(numUnit, param.getCompanyId());
            numUnitService.save(unit);
            asset.setNumUnitId(unit.getId());
        }
    }
}
