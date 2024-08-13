package com.bbs.financial.dto;

import com.bbs.financial.enums.TaxBurdenCalItemEnum;
import com.bbs.financial.enums.TaxBurdenCalTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 税负测算
 */
@Data
@ApiModel("税负测算")
public class TaxBurdenDto {

    /**
     * 销项
     */
    private List<TaxBurdenBase> saleList;

    /**
     * 进项
     */
    private List<TaxBurdenBase> inList;

    /**
     * 应交增值税
     */
    private List<TaxBurdenBase> vatList;

    /**
     * 附加税
     */
    private List<TaxBurdenBase> plusTaxList;

    @Data
    @ApiModel("税负测算基础")
    public static class TaxBurdenBase {
        /**
         * 税负测算唯一标识符
         */
        private Long id;

        /**
         * 项目名称
         */
        private String item;

        /**
         * 张数
         */
        private Integer num;

        /**
         * (不含税)金额
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal money;

        /**
         * 税额
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal taxMoney;

        /**
         * 排序: {@link TaxBurdenCalItemEnum}
         */
        private Integer sort;

        /**
         * 税负测算类型: {@link TaxBurdenCalTypeEnum}
         */
        private Integer type;
    }
}