package com.bbs.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 公司架构
 * @TableName company_structure
 */
@TableName(value ="company_structure")
@Data
public class CompanyStructure implements Serializable {
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
     * 单位名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 上级单位
     */
    @TableField(value = "pid")
    private Long pid;

    /**
     * 单位介绍
     */
    @TableField(value = "introduction")
    private String introduction;

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
     * 权重
     */
    @TableField(value = "weight")
    private Integer weight;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}