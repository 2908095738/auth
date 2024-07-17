package com.bbs.financial.api.close.balance;

import cn.hutool.core.date.DateUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.bbs.financial.entity.Certificate;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.entity.CertificateFile;
import com.bbs.financial.service.CertificateAbstractService;
import com.bbs.financial.service.CertificateService;
import com.bbs.financial.service.LedgerGeneralService;
import com.bbs.financial.util.LoginUser;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;

/**
 * 资产负债表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceSheet {

    /**
     * 资产列表
     */
    private List<Item> assetsList;

    /**
     * 负债列表
     */
    private List<Item> liabilitiesList;

    /**
     * 负债总计
     */
    private Long liabilitiesCount;

    /**
     * 所有者权益列表
     */
    private List<Item> ownersEquityList;

    /**
     * 所有者权益总计
     */
    private Long ownersEquityCount;

    /**
     * 负债和所有者权益总计
     */
    private Long liabilitiesAndOwnersEquityCount;

    /**
     * 是否平衡
     */
    private Boolean isBalance;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {

        private String name;

        private Long money;
    }


    private static BalanceSheet generateBalanceSheet(Date date) {
        LedgerGeneralService ledgerGeneralService = SpringUtil.getBean(LedgerGeneralService.class);

        /*
         * 银行存款 + 其他货币资金 + 库存现金
         */
        List<Long> accountIds = Arrays.asList(2L, 5L, 1L);

        return new BalanceSheet();
    }
}
