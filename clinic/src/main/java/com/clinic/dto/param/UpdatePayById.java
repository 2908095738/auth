package com.clinic.dto.param;

import com.clinic.enums.PayStateEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class UpdatePayById {
    /**
     * 门诊日志id
     */
    @NotNull(message = "日志id不能为空！")
    private Long admissionId;
    /**
     * id
     */
    @NotNull(message = "主键不能为空！")
    private Long id;
    /**
     * 全部费用
     */
    @NotNull(message = "全部费用不能为空！")
    private BigDecimal fee;
    /**
     * 支付状态
     */
    private Integer state = PayStateEnum.IS_PAY.getCode();
    /**
     * 收费方式
     */
    @NotNull(message = "收费方式不能为空！")
    private Integer way;
    /**
     * 备注
     */
    private String remark;

}
