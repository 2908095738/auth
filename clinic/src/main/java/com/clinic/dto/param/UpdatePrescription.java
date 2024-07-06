package com.clinic.dto.param;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdatePrescription {

    /**
     *
     */
    @NotNull(message = "主键不能为空！")
    private Long id;

    /**
     * 病人编号
     */
    @NotNull(message = "病人编号不能为空！")
    private Long patientId;

    /**
     * 支付编号
     */
    @NotNull(message = "支付编号不能为空！")
    private Long payId;

    /**
     * 药物过敏史
     */
    private String drugAllergyHistory;

    /**
     * 体重
     */
    private Double weight;

    /**
     * 体温
     */
    private Double temperature;

    /**
     * 血压
     */
    private Integer bloodPressure;

    /**
     * 血糖
     */
    private Double bloodGlucose;

    /**
     * 总价，药方药品总价之和
     */
    @NotNull(message = "总价不能为空！")
    private BigDecimal price;

    /**
     * 备注
     */
    private String remark;
    @Valid
    @NotNull
    private List<UpdatePrescriptionDrug> drugList;

}
