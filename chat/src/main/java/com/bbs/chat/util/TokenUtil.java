package com.bbs.chat.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bbs.Result;
import com.bbs.enums.APIEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import static cn.hutool.http.HttpStatus.*;

@Slf4j
@Configuration
public class TokenUtil {

    @Value("${tokenName}")
    private String tokenName;

    @Value("${authServerIP}")
    private String authServerIP;

    @Value("${authServerPort}")
    private String authServerPort;

    /**
     * 验证token是否正确
     * @param token token
     * @return 返回用户信息
     *
     */
    public Boolean verifyToken(String token) {
        JSONObject json = new JSONObject();
        json.put(tokenName,token);
        HttpResponse get = HttpRequest.get("http://"+authServerIP +":"+ authServerPort + APIEnum.REQUEST_VERIFY.getPath())
                .header(tokenName,token)
                .timeout(3000)
                .execute();
        switch (get.getStatus()) {
            case HTTP_OK:
                String body = get.body();
                Result result = JSON.parseObject(body, Result.class);
                if(result.getCode()!=HTTP_OK){
                    return false;
                }
//                JSONObject userJSONObj = (JSONObject) result.getData();
                log.debug("[TokenUtil::verifyToken] 响应内容={}; 解析结果={}", body, result);
                return true;
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
        return false;
    }


}
