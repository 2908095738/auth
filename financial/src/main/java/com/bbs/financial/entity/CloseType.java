package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 结账具体项
 * @TableName close_item
 */
@TableName(value ="close_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CloseType implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 公司ID
     */
    @TableField(value = "accounting_set_id")
    private Long accountingSetId;

    /**
     * 结账项编码
     */
    @TableField(value = "type_code")
    private String typeCode;

    /**
     * 结账项名称
     */
    @TableField(value = "type_name")
    private String typeName;

    /**
     * 最近一次计算金额
     */
    @TableField(value = "money")
    private Long money;

    /**
     * 是否需要生成凭证
     */
    @TableField(value = "is_generate_certificate")
    private Integer isGenerateCertificate;

    /**
     * 权重（排序用）
     */
    @TableField(value = "weight")
    private Integer weight;

    public CloseType(Long accountingSetId, String typeCode, String typeName, Long money, Integer isGenerateCertificate, Integer weight) {
        this.accountingSetId = accountingSetId;
        this.typeCode = typeCode;
        this.typeName = typeName;
        this.money = money;
        this.isGenerateCertificate = isGenerateCertificate;
        this.weight = weight;
    }

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}