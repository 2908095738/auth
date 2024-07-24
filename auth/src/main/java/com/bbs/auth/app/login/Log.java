package com.bbs.auth.app.login;

import com.bbs.auth.app.login.param.Param;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Log {

    /**
     * 登录结果（0正常/1失败）
     */
    private Integer result;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 输入验证码
     */
    private String code;

    /**
     * 登录类型
     */
    private String loginType;

    /**
     * 服务端保存的手机验证码
     */
    private Integer serverSavePhoneCode;

    /**
     * 登录成功生成的 Token
     */
    private String newToken;

    /**
     * 失败原因
     */
    private String errorMsg;

    /**
     * 登录时间
     */
    private String loginTime;

    public Log(Integer result, Param param, String loginTime) {
        this.result = result;
        this.phone = param.getPhone();
        this.code = param.getCode();
        this.loginType = param.getLoginType();
        this.loginTime = loginTime;
    }
}
