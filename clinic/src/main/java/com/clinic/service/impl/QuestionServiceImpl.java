package com.clinic.service.impl;

import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.clinic.entity.Question;
import com.clinic.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;

/**
 *
 */
@Slf4j
@Service
public class QuestionServiceImpl implements QuestionService{

    @Value("${answer.api.search.serverHost}")
    private String serverHost;

    @Value("${answer.api.search.timeout}")
    private Integer verifyTimeout;

    @Override
    public List<Question> search(String val) {
        List<Question> result = new ArrayList<>();
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("q", "is:question"+val);
            param.put("order", "relevance");
            HttpResponse response = HttpRequest.post(serverHost+"answer/api/v1/search").body(JSONUtil.toJsonPrettyStr(param))
                    .timeout(verifyTimeout).execute();
            if(response.isOk()) {
                String body = response.body();
                Result bean = JSONUtil.toBean(body, Result.class);
                log.debug(JSONUtil.toJsonPrettyStr(result));
                if(HttpStatus.HTTP_OK == bean.getCode()) {
                    Object data = bean.getData();
                    if(nonNull(data)) {
                        result = JSONUtil.parseArray(data).toList(Question.class);
                    }
                }
            }
            return result;
        } catch (HttpException e) {
            throw new RuntimeException(e);
        }
    }

    @lombok.Data
    private static class Result {

        private Integer code;

        private AnswerData data;

        private String msg;

        private String reason;

    }

    @lombok.Data
    private static class AnswerData {
        private String count;
        private List<ObjectList> list;
    }
    @lombok.Data
    private static class ObjectList {
        private String object_type;
        private Question object;
    }

}




