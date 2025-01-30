package com.auth.login.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.auth.exception.BusinessException;
import com.auth.user.dto.UserDTO;
import com.auth.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;


@Slf4j
@Component
public class WxConfig {

    @Value("${wx.oa.appId}")
    private String appId;
    @Value("${wx.oa.appSecret}")
    private String appSecret;
    @Resource
    private RedisUtil redisUtil;

    /**
     * 用于获取 AccessToken (微信接口调用凭证)
     * 由于微信限制每日只能获取 2000 次 AccessToken,使用缓存进行存储,避免接口调用次数过多
     * @return AccessToken
     */
    public String getAccessToken(){
        // 查询Redis,若存在则直接返回
        String accessToken = redisUtil.get("WEI_XIN_ACCESS_TOKEN");
        if (accessToken != null){
            return accessToken;
        }
        // 缓存不存在,向微信请求AccessToken
        Map<String, Object> params = new HashMap<>();
        params.put("appId", appId);
        params.put("secret", appSecret);
        params.put("grant_type", "client_credential");
        // 发送GET请求
        accessToken = HttpUtil.get("https://api.weixin.qq.com/cgi-bin/token?", params);
        // 处理结果
        JSONObject jsonObject = JSONObject.parseObject(accessToken);
        System.out.println(jsonObject);
        accessToken = jsonObject.getString("access_token");
        // 存入缓存
        if (StrUtil.isNotEmpty(accessToken) && accessToken != null){
            redisUtil.set("WEI_XIN_ACCESS_TOKEN",accessToken,60*60*2L);
        }else {
            log.info("微信返回的accessToken为空");
            throw new BusinessException("微信返回的accessToken为空");
        }
        return accessToken;
    }

    /**
     * 发送注册消息
     */
    public void sendRegisterMassage(String openId, UserDTO dbUser){
        log.debug("注册成功![Login::login] user={}", JSONUtil.toJsonPrettyStr(dbUser));
        // 获取 AccessToken
        String accessToken = getAccessToken();
        String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + accessToken;
        // 组织请求数据
        Map<String, Object> data = new HashMap<>();

        // 注册姓名
        Map<String, String> keyword3 = new HashMap<>();
        keyword3.put("value", dbUser.getPhone()+"");
        data.put("thing2",keyword3);
        // 注册手机号
        Map<String, String> keyword2 = new HashMap<>();
        keyword2.put("value", dbUser.getPhone()+"");
        data.put("phone_number1",keyword2);
        // 注册平台
        Map<String, String> keyword1 = new HashMap<>();
        keyword1.put("value", "诊所系统");
        data.put("thing4",keyword1);
        // 注册日期
        Map<String, String> keyword4 = new HashMap<>();
        keyword4.put("value", new SimpleDateFormat("yyyy年MM月dd日 HH:mm").format(new Date()));
        data.put("time5",keyword4);

        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("touser", openId);
        jsonData.put("template_id", "fJFF_ZdFeD-35UQnlWbcTiCVbPCfzmwjK28hGKlf4zU");
        jsonData.put("data", data);
        // 发送请求
        String result = HttpRequest.post(url).body(JSON.toJSONString(jsonData)).execute().body();
        // 结果处理
        JSONObject ticketJson = JSONObject.parseObject(result);
        Integer errcode = ticketJson.getInteger("errcode");
        if (errcode != 0){
            log.error("{}:{}", errcode, ticketJson.getString("errmsg"));
            throw new BusinessException("消息发送失败！");
        }
    }

    /**
     * 发送绑定消息
     */
    public void sendBindingMassage(String openId, UserDTO dbUser){
        log.debug("绑定成功![Login::login] user={}", JSONUtil.toJsonPrettyStr(dbUser));
        // 获取 AccessToken
        String accessToken = getAccessToken();
        String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + accessToken;
        // 组织请求数据
        Map<String, Object> data = new HashMap<>();

        // 姓名
        Map<String, String> keyword3 = new HashMap<>();
        keyword3.put("value", dbUser.getName());
        data.put("thing2",keyword3);
        // 绑定时间
        Map<String, String> keyword4 = new HashMap<>();
        keyword4.put("value", new SimpleDateFormat("yyyy年MM月dd日 HH:mm").format(new Date()));
        data.put("time4",keyword4);
        // 手机号
        Map<String, String> keyword2 = new HashMap<>();
        keyword2.put("value", dbUser.getPhone()+"");
        data.put("phone_number3",keyword2);

        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("touser", openId);
        jsonData.put("template_id", "adLInV-JT06goQSynlfrUBZl7MDLyHUrkrnVPd0F4Uw");
        jsonData.put("data", data);
        // 发送请求
        String result = HttpRequest.post(url).body(JSON.toJSONString(jsonData)).execute().body();
        // 结果处理
        JSONObject ticketJson = JSONObject.parseObject(result);
        Integer errcode = ticketJson.getInteger("errcode");
        if (errcode != 0){
            log.error("{}:{}", errcode, ticketJson.getString("errmsg"));
            throw new BusinessException("消息发送失败！");
        }
    }

}
