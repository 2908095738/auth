package com.bbs.financial.api.asset;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.bbs.financial.entity.Asset;
import com.bbs.financial.service.AssetService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping
public class DownloadImportTemplate {

    @Value("${asset.import.template.path}")
    private String templatePath;
    @Resource
    private HttpServletResponse response;

    @GetMapping("/asset/import/template/download")
    public void download(
            @RequestParam String templateName
    ) throws IllegalArgumentException {
        File template = loadTemplates(templateName);


        OutputStream out = null;
        ExcelWriter writer = ExcelUtil.getWriter(template);
        try {
            out = response.getOutputStream();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
            response.setHeader("content-disposition", "attachment;fileName=" + URLEncoder.encode(template.getName(), "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            writer.flush(out, true);
            writer.close();
            IoUtil.close(out);
        }
    }

    private File loadTemplates(String templateName) throws IllegalArgumentException {
        return Arrays.stream(FileUtil.ls(templatePath)).filter(file -> file.getName().equals(templateName))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("模板不存在"));
    }
}
