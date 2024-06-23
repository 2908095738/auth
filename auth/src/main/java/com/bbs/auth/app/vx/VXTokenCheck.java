package com.bbs.auth.app.vx;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping
public class VXTokenCheck {

    @Value("${vx.token}")
    private String token;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {

        /**
         * 微信加密签名，signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数。
         */
        private String signature;

        /**
         * 时间戳
         */
        private String timestamp;

        /**
         * 随机数
         */
        private String nonce;

        /**
         * 随机字符串
         */
        private String echostr;
    }

    @GetMapping("/vx/token/check")
    public String check(Param param) {
        String[] arr = new String[] { param.timestamp, param.nonce, token};
        Arrays.sort(arr);
        StringBuilder content = new StringBuilder();
        for (String str : arr) {
            content.append(str);
        }
        String tmpStr = DigestUtils.sha1Hex(content.toString());
        if (tmpStr.equals(param.signature)) {
            return param.echostr;
        } else {
            return "";
        }
    }
}
