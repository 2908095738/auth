package com.bbs.financial.api.asset;

import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.bbs.financial.entity.Asset;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@RestController
@RequestMapping
public class DownloadImportTemplate {

    @Resource
    private HttpServletResponse response;

    @GetMapping("/asset/import/template/download")
    public void download() throws IllegalArgumentException, IOException {
        ExcelWriter writer = ExcelUtil.getWriter();
        writer.write(Collections.singletonList(new Asset()), true);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.setHeader("Content-Disposition","attachment;filename=template.xls");
        ServletOutputStream out= response.getOutputStream();
        writer.flush(out, true);
        writer.close();
        IoUtil.close(out);
    }
}
