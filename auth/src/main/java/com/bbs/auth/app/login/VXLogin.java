package com.bbs.auth.app.login;

import com.bbs.auth.api.vx.GetAppID;
import com.bbs.auth.api.vx.GetSecret;
import com.bbs.auth.cache.user.UserCache;
import com.bbs.auth.cache.user.WXOpenIDCache;
import com.bbs.auth.entity.User;
import com.bbs.auth.api.vx.VXLoginAuthAPI;
import com.bbs.auth.entity.VXUser;
import com.bbs.auth.service.TokenService;
import com.bbs.Result;
import com.bbs.enums.LoginType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static com.bbs.Result.failed;
import static com.bbs.Result.success;
import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;

@RestController
@RequestMapping
public class VXLogin {

    private final UserCache cache;

    @Resource
    private WXOpenIDCache wxOpenIdCache;

    private final GetAppID getAppID;

    private final GetSecret getSecret;

    @Resource
    private TokenService tokenService;

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
         * 用户ID
         */
        private Long uid;
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
        if(LoginType.PHONE.getCode().equals(param.type)) {
            checkArgument(isNoneBlank(param.phone) && param.phone.length() == 11);
            User user = cache.searchOrRegisterByPhone(param.phone);
            String token = tokenService.verifyAndExpireToken(user);
            return success(new VO(user.getId(), user.getName(), token));

        } else if (LoginType.WX.getCode().equals(param.type)){
            checkArgument(isNoneBlank(param.code));
            String openid = VXLoginAuthAPI.getInstance(getAppID.get(), getSecret.get()).auth(param.code).getOpenid();
            try {
                VXUser vxUser = wxOpenIdCache.searchByOpenID(openid);
                String token = tokenService.verifyAndExpireToken(vxUser);
                return success(new VO(vxUser.getId(), vxUser.getName(), token));
            } catch (IllegalArgumentException e) {
                return success(401, "微信未绑定账号，请绑定账号后重试");
            }
        }
        return failed("登陆失败，请检查登录类型是否正确");
    }

    @Autowired
    public VXLogin(UserCache cache, GetAppID getAppID, GetSecret getSecret) {
        this.cache = cache;
        this.getAppID = getAppID;
        this.getSecret = getSecret;
    }
}
