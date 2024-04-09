package com.bbs.content.util;

import cn.hutool.http.*;
import cn.hutool.json.JSONUtil;
import com.bbs.Result;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.*;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
public class AuthUtil {

    @Slf4j
    @Component
    public static class UserAPI {

        @Value("${auth.api.verify.path}")
        private static String verifyApi;

        @Value("${auth.api.verify.token}")
        private static String verifyKey;

        @Value("${auth.api.verify.timeout}")
        private static Integer verifyTimeout;

        @Value("${auth.host}")
        private String host;

        @Resource
        private HttpServletRequest request;

        public User getLoginUser() {
            String token = request.getHeader(verifyKey);
            if(isNotBlank(token)) {
                String serverHost = host + verifyApi;
                try {
                    Map<String, Object> param = new HashMap<>();
                    param.put("token", token);
                    HttpResponse response = HttpRequest.post(serverHost).body(JSONUtil.toJsonPrettyStr(param))
                            .timeout(verifyTimeout).execute();
                    if(response.isOk()) {
                        String body = response.body();
                        Result result = JSONUtil.toBean(body, Result.class);
                        System.out.println(JSONUtil.toJsonPrettyStr(result));
                        if(HttpStatus.HTTP_OK == result.getCode()) {
                            Object data = result.getData();
                            if(nonNull(data)) {
                                User user = JSONUtil.parseObj(data).toBean(User.class);
                                System.out.println(data);
                                return user;
                            }
                        }
                    }
                    return null;

                } catch (HttpException e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class User {

            private Long id;

            private String name;

            private String email;

            private String phone;

            private String token;

            private Long failureTokenTime;
        }
    }
}
