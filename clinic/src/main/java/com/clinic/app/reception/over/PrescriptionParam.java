package com.clinic.app.reception.over;

import com.clinic.dto.param.SaveOrUpdatePrescriptionDrug;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 处方
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionParam {

    /**
     * 处方药品
     */
    @Valid
    @NotNull
    private List<SaveOrUpdatePrescriptionDrug> drugList;

    /**
     * 总价，药方药品总价之和
     */
    @NotNull(message = "总价不能为空！")
    private BigDecimal price;

    /**
     * 备注
     */
    private String remark;
}