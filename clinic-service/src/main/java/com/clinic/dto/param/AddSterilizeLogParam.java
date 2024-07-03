package com.clinic.dto.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddSterilizeLogParam {

    /**
     * 消毒时间
     */
    @NotNull
    private Date sterilizeTime;

    /**
     * 消毒部位
     */
    @NotNull
    @NotBlank
    private String content;

    /**
     * 消毒方法
     */
    @NotNull
    @NotBlank
    private String method;

    /**
     * 消毒剂
     */
    @NotNull
    @NotBlank
    private String disinfector;

    /**
     * 消毒人
     */
    @NotNull
    @NotBlank
    private String executor;
}
