package com.bbs.content.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
public class AuthUtil {

    @Slf4j
    @Component
    public static class UserAPI {

        @Value("${auth.api.verify.path}")
        private String verifyApi;

        @Value("${auth.api.verify.token}")
        private String verifyKey;

        @Value("${auth.api.search.id}")
        private String searchIdPath;

        @Value("${auth.api.search.list}")
        private String searchListPath;

        @Value("${auth.api.verify.timeout}")
        private Integer verifyTimeout;

        @Value("${auth.host}")
        private String host;

        @Resource
        private HttpServletRequest request;

        public User getLoginUser() {
            return getLoginUser(request.getHeader(verifyKey));
        }

        public User getLoginUser(String token) {
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

        public VO getUserByid(Long id) {
            if(Objects.nonNull(id)) {
                String serverHost = host + searchIdPath;
                try {
                    HttpResponse response = HttpRequest.get(serverHost+id)
                            .timeout(verifyTimeout).execute();
                    if(response.isOk()) {
                        String body = response.body();
                        Result result = JSONUtil.toBean(body, Result.class);
                        System.out.println(JSONUtil.toJsonPrettyStr(result));
                        if(HttpStatus.HTTP_OK == result.getCode()) {
                            Object data = result.getData();
                            if(nonNull(data)) {
                                VO vo = JSONUtil.parseObj(data).toBean(VO.class);
                                System.out.println(data);
                                return vo;
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

        public List<VO> getUserList(List<Long> ids) {
            if(CollUtil.isNotEmpty(ids)) {
                String serverHost = host + searchListPath;
                try {
                    Map<String, Object> param = new HashMap<>();
                    param.put("ids", ids);
                    HttpResponse response = HttpRequest.get(serverHost).form(param)
                            .timeout(verifyTimeout).execute();
                    if(response.isOk()) {
                        String body = response.body();
                        Result result = JSONUtil.toBean(body, Result.class);
                        System.out.println(JSONUtil.toJsonPrettyStr(result));
                        if(HttpStatus.HTTP_OK == result.getCode()) {
                            Object data = result.getData();
                            if(nonNull(data)) {
                                List<VO> voList = JSONUtil.toList(data.toString(),VO.class);
                                System.out.println(data);
                                return voList;
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
            /**
             * 头像 URL 地址
             */
            private String avatar;

            private String name;

            private String email;

            private String phone;

            private String token;

            private Long failureTokenTime;


        }



        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class VO {

            /**
             * 用户 ID
             */
             private Long id;
            /**
             * 用户昵称
             */
             private String name;
            /**
             * 头像 URL 地址
             */
             private String avatar;
            /**
             * 是否关注了当前登录用户
             */
             private Boolean isFollow;

        }

    }
}
