package com.clinic.app.wx.pay;

import com.wechat.pay.java.core.exception.ValidationException;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
public class WxPayCallBackApi {

    @Resource
    private WxPaySdkConfig config;

    private NotificationParser parser;


    /**
     *支付成功通知
     */
    @PostMapping("/weixin/pay/notification")
    public ResponseEntity notification(HttpServletRequest request, @RequestBody String body) {
        parser = new NotificationParser((NotificationConfig) config.getWxMlConfig());
        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(request.getHeader("Wechatpay-Serial"))
                .nonce(request.getHeader("Wechatpay-Nonce"))
                .signature(request.getHeader("Wechatpay-Signature"))
                .timestamp(request.getHeader("Wechatpay-Timestamp"))
                .body(body)
                .build();
        Transaction transaction;
        try {
            transaction = parser.parse(requestParam, Transaction.class);
        } catch (ValidationException e) {
            log.error("sign verification failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("transaction: {}", transaction);
        // 如果处理失败，应返回 4xx/5xx 的状态码，例如 500 INTERNAL_SERVER_ERROR
        if (true) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        // 处理成功，返回 200 OK 状态码
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
