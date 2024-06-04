package com.bbs.financial.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 科目名称
 */
@Data
@ApiModel("科目名称")
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
     * 会计科目名称
     */
    private String accountName;
}