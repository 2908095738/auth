package com.clinic.app.wx.pay;

import com.bbs.Result;
import com.wechat.pay.java.core.exception.HttpException;
import com.wechat.pay.java.core.exception.MalformedMessageException;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.CloseOrderRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByIdRequest;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class WxPaySdkApi {

    private NativePayService service;

    @Resource
    private WxPaySdkConfig config;


    @GetMapping("/weixin/pay/qrcode")
    public Result<Map<String, Object>> getPayCode() {
        // 初始化服务
        service = new NativePayService.Builder().config(config.getWxMlConfig()).build();
        try {
            String orderId = "tradeNo_"+System.currentTimeMillis()/1000+"_"+config.merchantId;
            PrepayResponse prepay = prepay(orderId);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("orderId", orderId);
            resultMap.put("codeUrl", prepay.getCodeUrl());
            return Result.success(resultMap);
        } catch (HttpException e) { // 发送HTTP请求失败
            log.error("请求失败", e);
            // 调用e.getHttpRequest()获取请求打印日志或上报监控，更多方法见HttpException定义
        } catch (ServiceException e) { // 服务返回状态小于200或大于等于300，例如500
            log.error("服务失败", e);
            // 调用e.getResponseBody()获取返回体打印日志或上报监控，更多方法见ServiceException定义
        } catch (MalformedMessageException e) { // 服务返回成功，返回体类型不合法，或者解析返回体失败
            log.error("解析失败", e);
            // 调用e.getMessage()获取信息打印日志或上报监控，更多方法见MalformedMessageException定义
        }
        return Result.failed("支付失败");
    }

    @GetMapping("/weixin/pay/WxOrder")
    public Result<Transaction> getByWxOrder(String orderId) {
        // 初始化服务
        service = new NativePayService.Builder().config(config.getWxMlConfig()).build();
        try {
            Transaction transaction = queryOrderByOutTradeNo(orderId);
            return Result.success(transaction);
        } catch (HttpException e) { // 发送HTTP请求失败
            log.error("请求失败", e);
            // 调用e.getHttpRequest()获取请求打印日志或上报监控，更多方法见HttpException定义
        } catch (ServiceException e) { // 服务返回状态小于200或大于等于300，例如500
            log.error("服务失败", e);
            // 调用e.getResponseBody()获取返回体打印日志或上报监控，更多方法见ServiceException定义
        } catch (MalformedMessageException e) { // 服务返回成功，返回体类型不合法，或者解析返回体失败
            log.error("解析失败", e);
            // 调用e.getMessage()获取信息打印日志或上报监控，更多方法见MalformedMessageException定义
        }
        return Result.failed("支付失败");
    }

    /** 如果支付退款，需要关闭订单 */
    public void closeOrder() {
        CloseOrderRequest request = new CloseOrderRequest();
        // 调用request.setXxx(val)设置所需参数，具体参数可见Request定义
        // 调用接口
        service.closeOrder(request);
    }

    /**
     * Native支付预下单
     */
    public PrepayResponse prepay(String outTradeNo) {
        PrepayRequest request = new PrepayRequest();
        request.setAppid(config.appId);
        request.setMchid(config.merchantId);
        Amount amount = new Amount();
        amount.setTotal(1);
        request.setAmount(amount);
        request.setDescription("码良科技-支付-诊所系统");
        request.setAttach("码良科技-支付-诊所系统");
        request.setNotifyUrl("https://maliang.work/api/weixin/pay/notification");//回调地址
        request.setOutTradeNo(outTradeNo);//商户订单号
        // 调用接口
        return service.prepay(request);
    }

    /** 微信支付订单号查询订单 */
    public Transaction queryOrderById() {
        QueryOrderByIdRequest request = new QueryOrderByIdRequest();
        request.setTransactionId("transaction_id_001");
        request.setMchid(config.merchantId);

        return service.queryOrderById(request);
    }

    /** 商户订单号查询订单 */
    public Transaction queryOrderByOutTradeNo(String orderId) {
        QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
        request.setOutTradeNo(orderId);
        request.setMchid(config.merchantId);
        return service.queryOrderByOutTradeNo(request);
    }



}
