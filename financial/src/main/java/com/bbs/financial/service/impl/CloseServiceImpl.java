package com.bbs.financial.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.financial.entity.Close;
import com.bbs.financial.entity.CloseType;
import com.bbs.financial.service.CloseService;
import com.bbs.financial.mapper.AssetCloseMapper;
import com.bbs.financial.service.CloseTypeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.math.NumberUtils.*;
import static org.apache.commons.lang3.math.NumberUtils.LONG_ZERO;

/**
* @author 路晨霖
* @description 针对表【asset_close(结账)】的数据库操作Service实现
* @createDate 2024-06-25 17:10:57
*/
@Service
public class CloseServiceImpl extends ServiceImpl<AssetCloseMapper, Close>
    implements CloseService {

    @Resource
    private CloseTypeService closeTypeService;
    @Override
    public List<CloseType> searchCloseType(Long accountingSetId) {
        List<CloseType> closeTypeList = closeTypeService.lambdaQuery()
                .eq(CloseType::getAccountingSetId, accountingSetId)
                .list();
        if(isNull(closeTypeList) || closeTypeList.size() == INTEGER_ZERO) {
            closeTypeList = loadCloseTypeByDefault(accountingSetId);
        }
        return closeTypeList;
    }
    /**
     * 通过默认结账类型配置，设置当前公司的结账类型配置
     */
    public List<CloseType> loadCloseTypeByDefault(Long accountingSetId) {
        List<CloseType> defaultCloseTypeList = searchDefaultCloseTypeList();
        defaultCloseTypeList.forEach(assetCloseType -> {
            assetCloseType.setId(null);
            assetCloseType.setAccountingSetId(accountingSetId);
        });
        closeTypeService.saveBatch(defaultCloseTypeList);
        return closeTypeService.lambdaQuery()
                .eq(CloseType::getAccountingSetId, accountingSetId)
                .list();
    }

    /**
     * 查询默认的结账项类型
     */
    public List<CloseType> searchDefaultCloseTypeList() {
        List<CloseType> defaultCloseTypeList = closeTypeService.lambdaQuery()
                .eq(CloseType::getAccountingSetId, LONG_ZERO)
                .list();
        if(isNull(defaultCloseTypeList) || defaultCloseTypeList.size() == INTEGER_ZERO) {
            closeTypeService.saveBatch(Arrays.asList(
                    new CloseType(LONG_ZERO, "asset_depreciation", "计提折旧", LONG_ZERO, INTEGER_ONE, INTEGER_ZERO),
                    new CloseType(LONG_ZERO, "transfer_out_unpaid_vat", "转出未交增值税", LONG_ZERO, INTEGER_ZERO, INTEGER_ONE),
                    new CloseType(LONG_ZERO, "provision_of_additional_taxes", "计提附加税", LONG_ZERO, INTEGER_ZERO, INTEGER_TWO),
                    new CloseType(LONG_ZERO, "cost_of_sales_carried_forward", "结转销售成本", LONG_ZERO, INTEGER_ONE, 3),
                    new CloseType(LONG_ZERO, "loss_and_gain_brought_forward", "结转损益", LONG_ZERO, INTEGER_ONE, 4)
            ));
            defaultCloseTypeList = closeTypeService.lambdaQuery()
                    .eq(CloseType::getAccountingSetId, LONG_ZERO)
                    .list();
        }
        return defaultCloseTypeList;
    }
}




