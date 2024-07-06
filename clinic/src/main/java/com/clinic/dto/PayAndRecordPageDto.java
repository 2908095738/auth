package com.clinic.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 全部收费列表返回
 */
@Data
public class PayAndRecordPageDto {

    /**
     * 就诊时间，既创建病例时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date dossierTime;

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别
     */
    private Integer sex;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 手机号
     */
    private Long phone;

    /**
     * 住址
     */
    private String address;

    /**
     * 费用
     */
    private BigDecimal fee;

    private List<PayRecordDto> payRecordList;

}
