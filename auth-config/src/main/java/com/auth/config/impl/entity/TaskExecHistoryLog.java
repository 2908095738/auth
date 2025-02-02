package com.auth.config.impl.entity;

import com.auth.config.enums.TaskExecResultEnum;
import com.auth.config.enums.TaskTimeUnitEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@TableName(value ="log_task_exec_history")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskExecHistoryLog {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     * @see TaskConfig#getId()
     * table config_time_task.id
     */
    @TableField(value = "task_id")
    private Long taskId;

    /**
     * 任务描述
     */
    @TableField(value = "task_desc")
    private String taskDesc;

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
     * 执行结果（0失败；1成功）
     * @see com.auth.config.enums.TaskExecResultEnum
     */
    @TableField(value = "exec_result")
    private Integer execResult;

    /**
     * 执行耗时
     */
    @TableField(value = "time")
    private Long time;

    /**
     * 耗时单位
     * @see TaskTimeUnitEnum#getCode()
     */
    @TableField(value = "time_unit")
    private Integer timeUnit;

    /**
     * 日志 traceId（用于查询打印的日志记录）
     */
    @TableField(value = "trace_id")
    private String traceId;

    /**
     * 执行时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    public TaskExecHistoryLog(TaskConfig taskConfig, TaskExecResultEnum execResultEnum, Long time, TaskTimeUnitEnum timeUnitEnum, String traceId) {
        this.taskId = taskConfig.getId();
        this.taskDesc = taskConfig.getTaskDescriptor();
        this.cron = taskConfig.getCron();
        this.classNameWithMethodName = taskConfig.getClassNameWithMethodName();
        this.execDescriptor = taskConfig.getExecDescriptor();
        this.execResult = execResultEnum.getExecResult();
        this.time = time;
        this.timeUnit = timeUnitEnum.getCode();
        this.traceId = traceId;
    }

    public TaskExecHistoryLog(TaskConfig taskConfig, TaskExecResultEnum execResultEnum, String traceId) {
        this.taskId = taskConfig.getId();
        this.taskDesc = taskConfig.getTaskDescriptor();
        this.cron = taskConfig.getCron();
        this.classNameWithMethodName = taskConfig.getClassNameWithMethodName();
        this.execDescriptor = taskConfig.getExecDescriptor();
        this.execResult = execResultEnum.getExecResult();
        this.traceId = traceId;
    }
}
