package com.auth.config.impl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 配置：需要 SuperAdmin 权限访问的 API 接口
 */
@TableName(value ="config_need_super_admin_api")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfigNeedSuperAdminApi {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * API Path
     */
    @TableField(value = "path")
    private String path;

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
}
