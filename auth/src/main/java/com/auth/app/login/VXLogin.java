package com.auth.app.login;

import com.auth.cache.UserCache;
import com.auth.entity.User;
import com.auth.util.VXLoginAuthAPI;
import com.clinic.Result;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.clinic.Result.success;
import static java.util.Objects.nonNull;

@RestController
@RequestMapping
public class VXLogin {

    private final UserCache cache;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class VXLoginParam {

        // 微信临时登录凭证
        // 与 appid、appSecret 一同，换取【用户唯一标识 OpenID】 、 用户在【微信开放平台】账号下的【唯一标识 UnionID】和【会话密钥 session_key】
        private String code;
    }

    /**
     * 微信小程序登录接口
     * @see <a href=https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/login.html>微信小程序登录流程</a>
     */
    @PostMapping("/login")
    public Result<User> login(@RequestBody VXLoginParam param) throws InterruptedException, IllegalArgumentException {
        String appSecret = "";
        String appId = "";
        VXLoginAuthAPI.Response auth = VXLoginAuthAPI.getInstance(appId, appSecret).auth(param.code);
        User user = cache.searchByOpenID(auth.getOpenid());
        return nonNull(user) ? success(user) : success(400, "小程序未绑定账号，尝试绑定已有账号或注册");
    }

    @Autowired
    public VXLogin(UserCache cache) {
        this.cache = cache;
    }
}
