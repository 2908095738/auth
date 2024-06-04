package com.bbs.financial.api.asset;

import cn.hutool.core.date.DateUtil;
import com.bbs.Result;
import com.bbs.financial.entity.Asset;
import com.bbs.financial.service.AssetService;
import com.bbs.financial.util.LoginUser;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
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
    private TransactionDefinition transactionDefinition;
    @Resource
    private DataSourceTransactionManager transactionManager;

    /**
     * 新增资产
     */
    @PutMapping("/asset")
    public Result<Long> add(@RequestBody Asset asset) {

        boolean isCreate = isCreate(asset);

        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            if(isCreate) {
                tryFillNo(asset);
                fillCreateUser(asset);
            } else {
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
                        (Objects.equals(count, LONG_ZERO) ? LONG_ONE : count)
        );
    }

    private void tryFillNo(Asset asset) {
        if(isSetAssetNo(asset)) generateAndFillNo(asset);
    }

    private void fillUpdateUser(Asset asset) {
        asset.setUpdateBy(LoginUser.getId());
    }
}
