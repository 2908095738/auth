package com.auth.login.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VO {
    /**
     * 用户ID
     */
    private Long uid;
    /**
     * 用户名称
     */
    private String name;

    private String token;

    /**
     * 是否设置了公司结构
     */
    Boolean isSettingCompanyStructure;

    public VO(Long uid, String name, String token) {
        this.uid = uid;
        this.name = name;
        this.token = token;
    }
}
