package com.bbs.content.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.event.Level;

import java.io.Serializable;
import java.util.Date;

/**
 * 操作日志
 * @TableName log_operation
 */
@TableName(value ="log_operation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationLog implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 记录时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 业务
     */
    @TableField(value = "service")
    private String service;

    /**
     * 位置
     */
    @TableField(value = "location")
    private String location;

    /**
     * 用户 ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 操作内容
     */
    @TableField(value = "operation")
    private String operation;

    /**
     * 级别
     */
    @TableField(value = "level")
    private Integer level;

    public OperationLog(String location, String service, String operation, Level level) {
        this.createTime = new Date();
        this.location = location;
        this.service = service;
//        this.userId = ;
        this.operation = operation;
        this.level = level.toInt();
    }

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}