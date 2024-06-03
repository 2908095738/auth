package com.bbs.financial.api.asset;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping
public class DownloadImportTemplate {

    @Value("${asset.import.template.path}")
    private String templatePath;

    @GetMapping("/asset/import/template/download")
    public void download(@RequestParam String templateName,  HttpServletResponse response) {
        List<File> templates = Arrays.stream(FileUtil.ls(templatePath)).filter(file -> file.getName().equals(templateName))
                .collect(Collectors.toList());

        templates.forEach(file -> {
            OutputStream out = null;
            ExcelWriter writer = ExcelUtil.getWriter(file);
            try {
                out = response.getOutputStream();
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
                response.setHeader("content-disposition", "attachment;fileName=" + URLEncoder.encode(file.getName(), "UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                writer.flush(out, true);
                writer.close();
                IoUtil.close(out);
            }
        });
    }
}
