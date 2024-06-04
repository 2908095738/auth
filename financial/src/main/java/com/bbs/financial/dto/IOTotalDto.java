package com.bbs.financial.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 收支汇总表
 */
@Data
@ApiModel("收支汇总表")
public class IOTotalDto extends BaseMoneyByCashierDto {

    /**
     * 账户编码
     */
    private String zhCode;

    /**
     * 账户名称
     */
    private String zhName;

    /**
     * 币别ID
     */
    private Long mTypeId;

    /**
     * 币别名称
     */
    private String mTypeName;

    /**
     * 科目id
     *  TODO L 前端无用字段，用于后端使用，后续剔除
     */
    private Long subjectsId;
}