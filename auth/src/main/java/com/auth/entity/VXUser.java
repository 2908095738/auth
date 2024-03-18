package com.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class VXUser extends User implements Serializable {

    /**
     * 会话密钥
     */
    private String session_key;

    /**
     * 用户唯一标识
     */
    private String openid;
}
