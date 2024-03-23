package com.auth.app.login;

import com.auth.api.vx.GetAppID;
import com.auth.api.vx.GetSecret;
import com.auth.cache.UserCache;
import com.auth.entity.User;
import com.auth.api.vx.VXLoginAuthAPI;
import com.auth.entity.VXUser;
import com.auth.service.TokenService;
import com.clinic.Result;
import com.clinic.enums.LoginType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.clinic.Result.failed;
import static com.clinic.Result.success;
import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;

@RestController
@RequestMapping
public class VXLogin {

    private final UserCache cache;

    private final GetAppID getAppID;

    private final GetSecret getSecret;

    private final TokenService tokenService;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class VXLoginParam {
        // 微信临时登录凭证
        // 与 appid、appSecret 一同，换取【用户唯一标识 OpenID】 、 用户在【微信开放平台】账号下的【唯一标识 UnionID】和【会话密钥 session_key】
        private String code;

        /**
         * 手机号
         */
        private String phone;
        /**
         * 登录类型：0 手机号/1 微信
         */
        private Integer type;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class VO {

        /**
         * 用户名称
         */
        private String name;

        private String token;
    }

    /**
     * 微信小程序登录接口
     * @see <a href=https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/login.html>微信小程序登录流程</a>
     */
    @PostMapping("/vx/login")
    public Result<VO> login(@RequestBody VXLoginParam param) throws InterruptedException {
        checkArgument(LoginType.checkFormat(param.type));
        checkArgument(isNoneBlank(param.code) && isNoneBlank(param.phone) && param.phone.length() == 11);
        try {
            if(LoginType.PHONE.getCode().equals(param.type)) {
                checkArgument(isNoneBlank(param.phone) && param.phone.length() == 11);
                User user = cache.searchByPhone(param.phone);
                String token = tokenService.createToken(user);
                return success(new VO(user.getName(), token));

            } else if (LoginType.WX.getCode().equals(param.type)){
                checkArgument(isNoneBlank(param.code));
                String openid = VXLoginAuthAPI.getInstance(getAppID.get(), getSecret.get()).auth(param.code).getOpenid();
                VXUser vxUser = cache.searchByOpenID(openid);
                String token = tokenService.createToken(vxUser);
                return success(new VO(vxUser.getName(), token));
            }
            return failed("登陆失败，请检查登录类型是否正确");
        } catch (IllegalArgumentException e) {
            return failed(401, "未绑定用户信息，请重新绑定");
        }
    }

    @Autowired
    public VXLogin(UserCache cache, GetAppID getAppID, GetSecret getSecret, TokenService tokenService) {
        this.cache = cache;
        this.getAppID = getAppID;
        this.getSecret = getSecret;
        this.tokenService = tokenService;
    }
}
