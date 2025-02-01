package com.auth.config.impl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName(value ="config_time_task")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskConfig {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 配置编码
     */
    @TableField(value = "code")
    private String code;

    /**
     * cron 表达式
     */
    @TableField(value = "cron")
    private String cron;

    /**
     * 类名和方法名表达式
     * 格式：com.auth.test.StrUtil#isEmpty
     */
    @TableField(value = "class_name_with_method_name")
    private String classNameWithMethodName;

    /**
     * 执行时间描述
     */
    @TableField(value = "exec_descriptor")
    private String execDescriptor;

    /**
     * 任务描述
     */
    @TableField(value = "task_descriptor")
    private String taskDescriptor;

    /**
     * 状态
     */
    @TableField(value = "state")
    private Integer state;
}
