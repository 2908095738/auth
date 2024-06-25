package com.bbs.financial.dto;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 科目名称
 */
@Data
@ApiModel("科目名称")
@NoArgsConstructor
@AllArgsConstructor
public class SubjectsNameDto {

    /**
     * 主键
     */
    private Long id;

    /**
     * 编号
     */
    private String no;

    /**
     * 名称
     */
    private String name;
}