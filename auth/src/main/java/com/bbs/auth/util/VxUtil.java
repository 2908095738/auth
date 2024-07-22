package com.bbs.auth.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.bbs.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@Slf4j
@Component
public class VxUtil {

    private String appId = "自己的appId";
    private String appSecret = "自己的appSecret";

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
     * 读取 Request Body 内容作为字符串
     * @param request HttpServletRequest
     * @return XmlString
     * @throws IOException XmlIO
     */
    public String readRequest(HttpServletRequest request) throws IOException {
        StringBuffer sb = new StringBuffer();
        InputStream inputStream = request.getInputStream();
        String str;
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        while ((str = in.readLine()) != null) {
            sb.append(str);
        }
        in.close();
        inputStream.close();
        return sb.toString();
    }

    /**
     * 将微信获取的XML结果转为Map
     * @param xmlString xml
     * @return Map
     * @throws DocumentException DocumentException
     */
    public Map<String, String> ResponseXmlToMap(String xmlString) throws DocumentException {
        // 解析 XML 字符串为 Document 对象
        Document document = DocumentHelper.parseText(xmlString);
        // 获取根元素
        Element rootElement = document.getRootElement();
        // 获取子元素
        List<Element> nodes = rootElement.elements();
        // 获取子元素的文本内容
        Map<String, String> resultMap = new HashMap<>();
        for (Node node:nodes ) {
            Element element = (Element) node;
            String nodeName = element.getName();
            String nodeText = element.getTextTrim();
            resultMap.put(nodeName, nodeText);
        }
        if (CollUtil.isEmpty(resultMap) && resultMap.containsKey("errcode")) {
            throw new BusinessException("系统异常");
        }
        return resultMap;
    }


    private static final String token = "nKjjt1fBXVxyyLC4"; //这个token值要和服务器配置一致

    public static boolean checkSignature(String signature, String timestamp, String nonce) {

        String[] arr = new String[]{token, timestamp, nonce};
        // 排序
        Arrays.sort(arr);
        // 生成字符串
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            content.append(arr[i]);
        }

        // sha1加密
        String temp = getSHA1String(content.toString());

        return temp.equals(signature); // 与微信传递过来的签名进行比较
    }

    private static String getSHA1String(String data) {
        // 使用commons codec生成sha1字符串
        return DigestUtils.shaHex(data);
    }



    /**
     * 发送卡片消息
     */
    public void sendLoginMassage(String openId,String userName,String templateId){
        // 获取 AccessToken
        String accessToken = getAccessToken();
        String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + accessToken;
        // 组织请求数据
        Map<String, Object> data = new HashMap<>();

        // 登录用户
        Map<String, String> keyword3 = new HashMap<>();
        keyword3.put("value", userName);
        data.put("模板信息.DATA前面的字符串",keyword3);
        // 登录时间
        Map<String, String> keyword4 = new HashMap<>();
        keyword4.put("value", new SimpleDateFormat("yyyy年MM月dd日 HH:mm").format(new Date()));
        data.put("模板信息.DATA前面的字符串",keyword4);
        // 登录网站
        Map<String, String> keyword1 = new HashMap<>();
        keyword1.put("value", "网站名称");
        data.put("模板信息.DATA前面的字符串",keyword1);
        // 登录网址
        Map<String, String> keyword2 = new HashMap<>();
        keyword2.put("value", "www.123.com");
        data.put("模板信息.DATA前面的字符串",keyword2);

        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("touser", openId);
        jsonData.put("template_id", templateId);
        // jsonData.put("client_msg_id", openId); // 防重入id（对于同一个openid + client_msg_id, 10分钟内只发送一条消息）
        jsonData.put("data", data);
        // 发送请求
        String result = HttpRequest.post(url).body(JSON.toJSONString(jsonData)).execute().body();
        // 结果处理
        JSONObject ticketJson = JSONObject.parseObject(result);
        Integer errcode = ticketJson.getInteger("errcode");
        if (errcode != 0){
            log.error(errcode + ":" + ticketJson.getString("errmsg"));
            throw new BusinessException("消息发送失败！");
        }
    }

}
