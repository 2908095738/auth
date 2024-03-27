package com.bbs.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.clinic.Result;
import com.bbs.enums.APIEnum;
import com.bbs.entity.UserVO;
import lombok.extern.slf4j.Slf4j;

import static cn.hutool.http.HttpStatus.*;


@Slf4j
public class AuthUtil {

    public String tokenName;

    public String authServerIP;

    public String authServerPort;

    private static final String TOKEN = "token"; //token

    public AuthUtil(String tokenName, String authServerIP, String authServerPort) {
        this.tokenName = tokenName;
        this.authServerIP = authServerIP;
        this.authServerPort = authServerPort;
    }

    public AuthUtil() {
    }
    /**
     * 验证token是否正确
     * @param token token
     * @return 返回用户信息
     *
     */
    public UserVO verifyToken(String token) {
        JSONObject json = new JSONObject();
        json.put(TOKEN,token);
        HttpResponse post = HttpRequest.post("http://"+authServerIP +":"+ authServerPort + APIEnum.REQUEST_VERIFY.getPath())
                .body(json.toString())
                .timeout(3000)
                .execute();
        switch (post.getStatus()) {
            case HTTP_OK:
                String body = post.body();
                Result result = JSON.parseObject(body, Result.class);
                JSONObject userJSONObj = (JSONObject) result.getData();
                UserVO user = JSONObject.toJavaObject(userJSONObj, UserVO.class);
                log.debug("[AuthUtil::verifyToken] 响应内容={}; 解析结果={}; userVO={}", body, result, user);
                return user;
            case HTTP_BAD_REQUEST:
                log.debug("用户 Token 解析失败");
                break;
            case HTTP_NOT_FOUND:
                log.error("Auth-SDK 无法调用 Auth 服务，请检查网络情况！！！");
                break;
            case HTTP_INTERNAL_ERROR:
                log.error("Auth 服务异常！！！");
                break;
        }
        return null;
    }



}
