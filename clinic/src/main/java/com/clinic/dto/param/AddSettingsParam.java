package com.clinic.dto.param;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AddSettingsParam {

    /**
     * 过期预提醒时间（月）
     */
    @NotNull(message = "过期预提醒时间（月）不能为空")
    private Integer expiryAlertMonth;

    /**
     * 库存统计规则
     */
    @NotNull(message = "库存统计规则不能为空")
    private Integer stateCountRule;

    /**
     * 统计值（统计方式值，如百分比 10%； 数量）
     */
    @NotNull(message = "统计值不能为空")
    private Integer countVal;

    /**
     * 统计单位(0最小单位1最大单位)
     */
    @NotNull(message = "统计单位不能为空")
    private Integer countUnit;


    /**
     * 科别：内科，中西医结合，中医
     */
    @NotNull(message = "科别不能为空")
    private String division;

    /**
     * 诊所名称
     */
    @NotNull(message = "诊所名称不能为空")
    private String clinicName;


}
