package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * 记账凭证
 * @TableName certificate
 */
@TableName(value ="certificate")
@Data
public class Certificate implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 公司ID
     */
    @TableField(value = "company_id")
    private Long companyId;

    /**
     * 凭证字
     */
    @TableField(value = "certificate_word")
    private String certificateWord;

    /**
     * 编号（凭证号）
     */
    @TableField(value = "no")
    private Long no;

    /**
     * 日期
     */
    @TableField(value = "date")
    private Date date;

    /**
     * 权重
     */
    @TableField(value = "weight")
    private Integer weight;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 信息创建人
     */
    @TableField(value = "create_by")
    private Long createBy;

    /**
     * 修改时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 信息修改人
     */
    @TableField(value = "update_by")
    private Long updateBy;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


    @TableField(exist = false)
    private List<CertificateAbstract> abstracts;

    @TableField(exist = false)
    private List<CertificateFile> files;
}