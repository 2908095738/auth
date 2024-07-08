package com.bbs.financial.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 日记账数据
 */
@Data
@ApiModel("日记账数据")
public class NoteDto {
    /**
     * 日记账唯一标识符
     */
    private Long id;

    /**
     * 日期
     */
    private Date date;

    /**
     * 摘要
     */
    private String certificateAbstract;

    /**
     * 币别名称
     */
    private String moneyName;

    /**
     * 账户名称
     */
    private String zhName;

    /**
     * 账户对方科目
     */
    private String heSubjName;

    /**
     * 期初余额
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal startMoney;

    /**
     * 收入
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal borrowMoney;

    /**
     * 支出
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal loansMoney;

    /**
     * 期末余额
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal surplusMoney;

    /**
     * 凭证
     */
    private String certName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 制单人
     */
    private String makeName;

    /**
     * 凭证ID
     */
    private Long certificateId;

    /**
     * 币别id
     */
    private Long moneyId;

    /**
     * 账户科目id
     */
    private Long zhSubjId;

    /**
     * 账户对方科目id
     */
    private Long heSubjId;
}