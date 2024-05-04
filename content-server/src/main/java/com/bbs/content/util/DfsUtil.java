package com.bbs.content.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.bbs.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DfsUtil {

        @Value("${dfs.api.deleteList}")
        private String deleteListPath;

        @Value("${dfs.api.timeout}")
        private Integer timeout;

        @Value("${dfs.host}")
        private String host;


        public boolean deleteList(List<String> resourceIDs) {
            if(CollUtil.isNotEmpty(resourceIDs)) {
                String serverHost = host + deleteListPath;
                try {
                    Map<String, Object> param = new HashMap<>();
                    param.put("resourceIds", resourceIDs);
                    HttpResponse response = HttpRequest.delete(serverHost).form("resourceIds", resourceIDs)
                            .timeout(timeout).execute();
                    if(response.isOk()) {
                        String body = response.body();
                        Result result = JSONUtil.toBean(body, Result.class);
                        log.debug(JSONUtil.toJsonPrettyStr(result));
                        if(HttpStatus.HTTP_OK == result.getCode()) {
                            return true;
                        }
                    }
                } catch (HttpException e) {
                    throw new RuntimeException(e);
                }
            }
            return false;
        }






}
