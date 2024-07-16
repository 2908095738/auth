package com.bbs.financial.api.close;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
}
