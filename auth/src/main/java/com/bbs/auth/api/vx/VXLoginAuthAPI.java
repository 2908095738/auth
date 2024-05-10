package com.bbs.auth.api.vx;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class VXLoginAuthAPI {

    private static final String LOGIN_API = "https://api.weixin.qq.com/sns/jscode2session";

    private final String appid;

    private final String secret;

    private volatile static VXLoginAuthAPI instance;

    private static final String APP_ID_KEY = "appid";

    private static final String SECRET_KEY = "secret";
    private static final String GRANT_TYPE_KEY = "grant_type";
    private static final String GRANT_TYPE = "authorization_code";

    private static final String JS_CODE_KEY = "js_code";

    public static final String OPEN_ID_KEY = "openid";

    public static final String SESSION_KEY = "session_key";

    private VXLoginAuthAPI(String appid, String secret) {
        this.appid = appid;
        this.secret = secret;
    }

    public static VXLoginAuthAPI getInstance(String appid, String secret) {
        if(instance == null) {
            synchronized (VXLoginAuthAPI.class) {
                if(instance == null) {
                    instance = new VXLoginAuthAPI(appid, secret);
                }
            }
        }
        return instance;
    }

    /**
     * 响应参数
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        /**
         * 会话密钥
         */
        private String session_key;

        /**
         * 用户唯一标识
         */
        private String openid;

        /**
         * 用户在开放平台的唯一标识符
         * 若当前小程序已绑定到微信开放平台账号下会返回，详见 UnionID 机制说明。
         */
        private String unionid;

        /**
         * 错误信息
         */
        private String errmsg;

        /**
         * 错误码
         */
        private Integer errcode;
    }

    /**
     * 登录凭证校验
     * 通过 wx.login 接口获得临时登录凭证 code 后传到开发者服务器调用此接口完成登录流程。更多使用方法详见小程序登录
     * @param jsCode 登录时获取的 code，可通过wx.login获取
     * @return 微信登录凭证
     * @see <a href=https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/code2Session.html>小程序登录接口</a>
     */
    public Response auth(String jsCode) {
        Map<String, Object> param = new HashMap<String, Object>() {{
            put(GRANT_TYPE_KEY, GRANT_TYPE);
            put(APP_ID_KEY, appid);
            put(SECRET_KEY, secret);
            put(JS_CODE_KEY, jsCode);
        }};
        String resStr = HttpUtil.get(LOGIN_API, param);
        if(StringUtils.isNotBlank(resStr)) {
            Response response = JSONUtil.toBean(resStr, Response.class);
            log.debug("请求微信登录凭证校验: param={}; response= {}", JSONUtil.toJsonPrettyStr(param), JSONUtil.toJsonPrettyStr(response));
            return response;
        }
        return null;
    }
}
