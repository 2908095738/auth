package com.auth.config.impl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName(value ="config_system")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemConfigItem {

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
     * 配置值
     */
    @TableField(value = "val")
    private String value;

    /**
     * 描述
     */
    @TableField(value = "descriptor")
    private String descriptor;

    /**
     * 状态
     */
    @TableField(value = "state")
    private Integer state;

    public Integer intValue() {
        return Integer.valueOf(value);
    }
}
