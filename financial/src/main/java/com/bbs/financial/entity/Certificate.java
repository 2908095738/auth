package com.bbs.financial.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bbs.api.auth.User;
import com.bbs.enums.financial.CertificateWordEnum;
import com.bbs.vo.Company;
import com.github.yulichang.annotation.EntityMapping;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


/**
 * 记账凭证
 * @TableName certificate
 */
@FieldNameConstants
@TableName(value ="certificate")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Certificate implements Serializable {
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
     * 凭证字
     */
    @TableField(value = "certificate_word")
    @JSONField(serialzeFeatures = SerializerFeature.WriteEnumUsingToString)
    private CertificateWordEnum certificateWord;

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
     * 信息审核人
     */
    @TableField(value = "auth_by")
    private Long authBy;

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

    /**
     * 凭证类型：1折旧凭证
     */
    @TableField(value = "type")
    private Integer type;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableField(exist = false)
    private User createUser;

    @TableField(exist = false)
    private User authUser;

    @TableField(exist = false)
    @EntityMapping(
            thisField = Fields.id,
            joinField = CertificateAbstract.Fields.certificateId
    )
    private List<CertificateAbstract> abstracts;

    @TableField(exist = false)
    @EntityMapping(
            thisField = Fields.id,
            joinField = CertificateFile.Fields.certificateId
    )
    private List<CertificateFile> files;

    @TableField(exist = false)
    private Company company;

    @TableField(exist = false)
    private Account account;
}