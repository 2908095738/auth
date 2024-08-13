package com.clinic.service.impl;

import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
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
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

/**
 *
 */
@Slf4j
@Service
public class QuestionServiceImpl implements QuestionService{

    @Value("${answer.api.search.serverHost}")
    private String serverHost;

    @Override
    public List<Question> search(String val) {
        List<Question> result = new ArrayList<>();
        List<ObjectList> objList =  new ArrayList<>();
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("q", "is:question"+val);
            param.put("order", "relevance");
            param.put("page", 1);
            param.put("size", 10);
            log.debug("param,{}",param);
            String body = HttpRequest.get(serverHost+"answer/api/v1/search").form(param).execute(true).body();
            Result bean = JSONUtil.toBean(body, Result.class);
            if(HttpStatus.HTTP_OK == bean.code) {
                Object data = bean.data;
                if(nonNull(data)) {
                    AnswerData dataBean = JSONUtil.parse(data).toBean(AnswerData.class);
                    objList = JSONUtil.toList(dataBean.getList(),ObjectList.class);
                }
            }
            if(!objList.isEmpty()){
                result = objList.stream().map(o-> JSONUtil.toBean(o.getObject(),Question.class)).collect(Collectors.toList());
            }
            return result;
        } catch (HttpException e) {
            throw new RuntimeException(e);
        }
    }

    @lombok.Data
    private static class Result {

        private Integer code;

        private Object data;

        private String msg;

        private String reason;

    }

    @lombok.Data
    private static class AnswerData {
        private Integer count;
        private JSONArray list;
    }

    @lombok.Data
    private static class ObjectList {
        private String object_type;
        private JSONObject object;
    }

}




