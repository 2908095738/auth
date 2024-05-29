package com.bbs.financial.api.asset;

import cn.hutool.core.date.DateUtil;
import com.bbs.Result;
import com.bbs.financial.converter.AssetConverter;
import com.bbs.financial.entity.Asset;
import com.bbs.financial.service.AssetService;
import com.bbs.financial.util.LoginUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

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
         * 录入月份
         */
        private String entryMonth;

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
        private Integer assetTypeId;

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
        private Long numUnit;

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
        private Long useId;

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
        private String assetsCleanMonth;

        /**
         * 状态:正常 清理
         */
        private Integer status;

        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 新增资产
     */
    @PutMapping("/asset")
    public Result<Boolean> add(@RequestBody Param param) {
        Asset asset = converter.toEntity(param);
        if(isNull(asset.getId())) {
            asset.setCreateBy(LoginUser.getId());
            // 如果未设置编码，则使用【公司ID + 日期 + 已有资产数量（去重）】当作默认编码
            if(isBlank(asset.getNo())) {
                Long count = assetService.lambdaQuery().eq(Asset::getCompanyId, asset.getCompanyId()).count();
                asset.setNo(
                        asset.getCompanyId() +
                        DateUtil.format(new Date(), "yyyyMMdd") +
                        (Objects.equals(count, LONG_ZERO) ? LONG_ONE : count)
                );
            }
        }
        return success(assetService.saveOrUpdate(asset));
    }
}
