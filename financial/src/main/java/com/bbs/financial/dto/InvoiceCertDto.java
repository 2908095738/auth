package com.bbs.financial.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 发票生成凭证预览
 */
@Data
@ApiModel("发票生成凭证预览")
public class InvoiceCertDto {

    /**
     * 发票
     */
    private InvoiceDto invoice;

    /**
     * 凭证模板名称
     */
    private String tempName;

    /**
     * 需要重新选择的辅助核算映射
     */
    private Map<Long, String> auxResetMapByType;

    private List<InvoiceCertAbst> absts;

    @Data
    @ApiModel("发票凭证摘要")
    public static class InvoiceCertAbst {

        /**
         * 唯一标识符
         */
        private Long id;

        /**
         * 摘要
         */
        private String certificateAbstract;

        /**
         * 编号
         */
        private String no;

        /**
         * 名称
         */
        private String name;

        /**
         * 辅助核算
         */
        private String auxiliary;

        /**
         * 借方金额
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal borrowMoney;

        /**
         * 贷方金额
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal loansMoney;

        /**
         * 取值类型：0.价税合计;1.税额;2.不含税金额;
         */
        private Integer moneyType;

        /**
         * 科目id
         */
        private Long accountId;

        /**
         * 辅助核算类型id
         */
        private Long auxTypeId;

        /**
         * 凭证摘要辅助核算id
         */
        private Long abstAuxId;
    }
}