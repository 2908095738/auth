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
public class AddDisinfectionLogParam {

    /**
     * 消杀日期
     */
    @NotNull
    private Date disinfectionTime;

    /**
     * 消杀内容
     */
    @NotBlank
    private String content;

    /**
     * 消杀药品、器械
     */
    @NotBlank
    private String items;

    /**
     * 消杀/营业时间范围
     */
    @NotNull
    private Date startTimeRange;

    /**
     * 消杀/营业时间范围
     */
    @NotNull
    private Date endTimeRange;

    /**
     * 消杀地点
     */
    @NotBlank
    private String spot;

    /**
     * 消杀人
     */
    @NotBlank
    private String executor;
}
