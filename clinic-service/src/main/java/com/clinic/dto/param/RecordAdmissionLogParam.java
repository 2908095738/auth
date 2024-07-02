package com.clinic.dto.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordAdmissionLogParam  {

    /**
     * 病人ID
     */
    @NotNull
    private Long patientId;

    /**
     * 初复诊（0初诊/1复诊）
     */
    private Integer isFirst;
}
