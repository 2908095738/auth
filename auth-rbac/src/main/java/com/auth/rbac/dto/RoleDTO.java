package com.auth.rbac.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {

    /**
     * 编号
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 系统编码
     */
    @TableField(value = "system_code")
    private String systemCode;

    /**
     * 编码
     */
    @TableField(value = "code")
    private String code;

    /**
     * 名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 状态（0正常、1不可用）
     */
    @TableField(value = "state")
    private Integer state;

    /**
     * 系统信息
     */
    @TableField(exist = false)
    private SystemDTO systemDTO;
}
