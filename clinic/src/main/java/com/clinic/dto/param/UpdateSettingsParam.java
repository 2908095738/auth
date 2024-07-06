package com.clinic.dto.param;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UpdateSettingsParam {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空")
    private Long id;

    /**
     * 过期预提醒时间（月）
     */
    private Integer expiryAlertMonth;

    /**
     * 库存统计规则
     */
    private Integer stateCountRule;

    /**
     * 统计值（统计方式值，如百分比 10%； 数量）
     */
    private Integer countVal;

    /**
     * 统计单位(0最小单位1最大单位)
     */
    private Integer countUnit;


    /**
     * 科别：内科，中西医结合，中医
     */
    private String division;

    /**
     * 诊所名称
     */
    private String clinicName;


}
