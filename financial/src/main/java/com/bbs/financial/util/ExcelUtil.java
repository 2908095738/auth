package com.bbs.financial.util;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Slf4j
public class ExcelUtil {
    //发送响应流方法
    public static void setResponseHeader(HttpServletRequest request, HttpServletResponse response, String fileName) throws UnsupportedEncodingException {
        response.setContentType("application/octet-stream;charset=UTF-8");
        String finalFileName = fileName;
        String userAgent = request.getHeader("user-agent");
        if (userAgent != null && userAgent.indexOf("Firefox") >= 0 || userAgent.indexOf("Chrome") >= 0 || userAgent.indexOf("Safari") >= 0) {
            finalFileName = new String((fileName).getBytes(), "ISO8859-1");
        } else {
            finalFileName = URLEncoder.encode(fileName, "UTF8"); //其他浏览器
        }
        response.setHeader("Content-Disposition", "attachment;filename=".concat(finalFileName));
        response.addHeader("Pargam", "no-cache");
        response.addHeader("Cache-Control", "no-cache");
    }
}