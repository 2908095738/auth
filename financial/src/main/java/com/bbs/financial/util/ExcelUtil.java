package com.bbs.financial.util;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelWriter;
import cn.hutool.poi.excel.StyleSet;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
public class ExcelUtil {
    /**
     * @param excelName     表格名称
     * @param initFrameFunc 初始化表格框架接口
     * @param initDataFunc  初始化表格数据接口
     */
    public static <R> void export(HttpServletRequest req, HttpServletResponse resp, String excelName, Consumer<ExcelWriter> initFrameFunc, Supplier<List<R>> initDataFunc) {
        OutputStream out = null;
        ExcelWriter writer = cn.hutool.poi.excel.ExcelUtil.getWriter(new String(getExcelName("export").getBytes(StandardCharsets.UTF_8)));
        try {
            out = resp.getOutputStream();
            setResponseHeader(req, resp, getExcelName(excelName));

            initFrameFunc.accept(writer);

            writer.write(initDataFunc.get(), true);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            writer.flush(out, true);
            writer.close();
            IoUtil.close(out);
        }
    }

    private static String getExcelName(String title) {
        DateTime now = DateTime.now();
        return String.format("%d%d%d_%d%d%d_%s.xlsx",
                now.year(), now.monthBaseOne(), now.dayOfMonth(),
                now.hour(true), now.minute(), now.second(),
                title
        );
    }

    //发送响应流方法
    public static void setResponseHeader(HttpServletRequest request, HttpServletResponse response, String fileName) throws UnsupportedEncodingException {
        //TODO L 中文乱码修正
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String userAgent = request.getHeader("user-agent");
        if (userAgent != null && userAgent.indexOf("Firefox") >= 0 || userAgent.indexOf("Chrome") >= 0 || userAgent.indexOf("Safari") >= 0) {
            fileName = new String((fileName).getBytes(), "ISO8859-1");
        } else {
            fileName = URLEncoder.encode(fileName, "UTF8"); //其他浏览器
        }
        response.setHeader("Content-Disposition", "attachment;filename=".concat(fileName));
        response.setHeader("Content-Security-Policy", "upgrade-insecure-requests");
        response.addHeader("Pargam", "no-cache");
        response.addHeader("Cache-Control", "no-cache");
        response.setHeader("filename", fileName);
    }

    /**
     * 单元格格式设置为文本
     */
    public static void setTextCell(ExcelWriter writer){
        StyleSet styleSet = writer.getStyleSet();
        CellStyle cellStyle = styleSet.getCellStyleForNumber();
        DataFormat format = writer.getWorkbook().createDataFormat();
        cellStyle.setDataFormat(format.getFormat("@"));
        writer.setStyleSet(styleSet);
    }
}