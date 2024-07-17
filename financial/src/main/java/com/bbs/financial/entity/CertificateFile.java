package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.util.Date;

/**
 * 记账凭证附件
 * @TableName certificate_file
 */
@TableName(value ="certificate_file")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class CertificateFile implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文件名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 公司ID
     */
    @TableField(value = "accounting_set_id")
    private Long accountingSetId;

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
     * 记账凭证ID
     */
    @TableField(value = "certificate_id")
    private Long certificateId;

    /**
     * 附件URL
     */
    @TableField(value = "url")
    private String url;

    /**
     * 资源编号
     */
    @TableField(value = "resource_id")
    private String resourceId;

    /**
     * 文件类型
     */
    @TableField(value = "file_type")
    private Integer fileType;

    /**
     * 文件类型
     */
    @TableField(value = "content_type")
    private String contentType;

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

    public CertificateFile(String name, Long accountingSetId, String certificateWord, Long no, Date date, String url, String resourceId, Integer fileType, String contentType, Long createBy) {
        this.name = name;
        this.accountingSetId = accountingSetId;
        this.certificateWord = certificateWord;
        this.no = no;
        this.date = date;
        this.url = url;
        this.resourceId = resourceId;
        this.fileType = fileType;
        this.contentType = contentType;
        this.createBy = createBy;
    }
}