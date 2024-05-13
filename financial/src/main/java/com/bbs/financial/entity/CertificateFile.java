package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 记账凭证附件
 * @TableName certificate_file
 */
@TableName(value ="certificate_file")
@Data
public class CertificateFile implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 记账凭证ID
     */
    @TableField(value = "certificate_id")
    private Long certificateId;

    /**
     * 附件URL
     */
    @TableField(value = "url")
    private Long url;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}