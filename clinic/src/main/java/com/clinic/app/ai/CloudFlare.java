package com.clinic.app.ai;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.bbs.Result;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import static java.util.Objects.nonNull;

@Slf4j
@RestController
@RequestMapping
public class CloudFlare {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {

        private String role;

        private String content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseResult {

        private String response;

        private Boolean success;

        private Object[] errors;

        private Object[] messages;
    }

    @GetMapping("/ai/cf")
    public Result<String> quiz(@RequestParam String quiz) {
        List<Message> messages = Arrays.asList(new Message(
                "system",
                "我在开发一个医疗软件，我会问你一些医疗相关问题或者需要软件中的某个功能、或者查询一些功能的数据，例如需要某个病人一段时间的就诊记录等或者需要某些相关文献。" +
                        "你的所有的回答都需要用中文，我的系统中有病人档案信息（病人信息）、就诊记录"
        ), new Message(
                "user",
                quiz
        ));
        TimeInterval timer = DateUtil.timer();
        String resultStr = HttpRequest.post("https://api.cloudflare.com/client/v4/accounts/f7c5e1e128be702e007f4eec33650795/ai/run/@cf/qwen/qwen1.5-14b-chat-awq")
                .header("Authorization", "Bearer Iu0h_tO72-4U0whjsFZ6T2egyD8jo4OHqMcEZrzM")//头信息，多个头信息多次调用此方法即可
                .body(JSONUtil.toJsonPrettyStr(new HashMap<String, Object>() {{
                    put("messages", messages);
                }}))//表单内容
                .timeout(60000)//超时，毫秒
                .execute().body();
        log.info("AI: resultStr={};", resultStr);
        if(StringUtils.isNotBlank(resultStr)) {
            Object resultObj = JSONUtil.parseObj(resultStr).get("result");
            if(nonNull(resultObj)) {
                String answer = JSONUtil.parseObj(resultObj).get("response").toString();
                log.info("AI: quiz={}; answer={}; timer={};", quiz, answer, timer.interval() / 1000);
                return Result.success(answer);
            }
        }
        log.error("AI: quiz={}; resultStr={}", quiz, resultStr);
        return Result.failed(resultStr);
    }
}
