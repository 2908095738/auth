package com.clinic.dto.param;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

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
    private Integer state;
    /**
     * 收费方式
     */
    private Integer way;
    /**
     * 备注
     */
    private String remark;

    @Valid
    private List<CreateOrSetPayRecord> payRecords;

}
