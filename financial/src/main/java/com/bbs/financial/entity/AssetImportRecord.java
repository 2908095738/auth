package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 工资导入记录
 * @TableName asset_import_record
 */
@TableName(value ="asset_import_record")
@Data
public class AssetImportRecord implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 导入结果（0成功，1失败）
     */
    @TableField(value = "result")
    private Integer result;

    /**
     * 导入数据量
     */
    @TableField(value = "size")
    private Integer size;

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
     * 失败原因
     */
    @TableField(value = "error_message")
    private String errorMessage;

    /**
     * 公司ID
     */
    @TableField(value = "company_id")
    private Long companyId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public AssetImportRecord(Long companyId, Long createBy) {
        this.companyId = companyId;
        this.createBy = createBy;
    }
}