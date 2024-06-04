package com.bbs.financial.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * 核对总账
 */
@Data
@ApiModel("核对总账")
public class ConfirmTotalDto {
    /**
     * 科目总账
     */
    private SubjDto subj;

    /**
     * 子科目列表
     */
    private List<BaseTotalDto> downSubj;

    /**
     * 差额
     */
    private BaseTotalDto lessTotal;

    /**
     * 基础总账实例
     */
    @Data
    @ApiModel("基础总账实例")
    public static class BaseTotalDto extends BaseMoneyByCashierDto {
        /**
         * 项目
         */
        private String projName;

        /**
         * 科目id
         */
        private Long subjectsId;
    }

    /**
     * 科目总账
     */
    @Data
    @ApiModel("科目总账")
    public static class SubjDto extends BaseTotalDto {
        /**
         * 编号
         */
        private String no;
    }
}