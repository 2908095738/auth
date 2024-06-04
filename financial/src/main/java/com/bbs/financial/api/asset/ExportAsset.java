package com.bbs.financial.api.asset;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.bbs.financial.entity.Asset;
import com.bbs.financial.service.AssetService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;

@RestController
@RequestMapping
public class ExportAsset {

    @Value("${asset.import.template.path}")
    private String templatePath;
    @Resource
    private AssetService db;
    @Resource
    private HttpServletResponse response;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {

        private Long startDate;

        private Long endDate;
    }

    @GetMapping("/asset/export")
    public void export(
            @RequestParam Long companyId,
            @RequestParam String templateName,
            Param param
    ) {
        File template = loadTemplates(templateName);

        Date startDate = null;
        if(nonNull(param.startDate)) {
            startDate = new Date(param.startDate);
        }

        Date endDate = null;
        if(nonNull(param.endDate)) {
            endDate = new Date(param.endDate);
        }

        Date finalStartDate = startDate;
        Date finalEndDate = endDate;
        List<Asset> assets = db.selectJoinList(Asset.class, new MPJLambdaWrapper<Asset>()
                .selectAll(Asset.class)
                .eq(Asset::getCompanyId, companyId)
                .or(nonNull(startDate), wrapper -> wrapper
                        .ge(Asset::getCreateTime, nonNull(finalStartDate) ? DateUtil.beginOfMonth(finalStartDate) : null)
                        // 最大时间使用传入的 endDate 取当月最后一天（如果只查单月，则 endDate 可空，并使用传入的 startDate 替换计算最后一天）
                        .lt(Asset::getCreateTime, nonNull(finalEndDate) ? DateUtil.offsetMonth(finalEndDate, INTEGER_ONE) : DateUtil.offsetMonth(finalStartDate, INTEGER_ONE))
                )
        );

        OutputStream out = null;
        ExcelWriter writer = ExcelUtil.getWriter(template);
        try {
            //跳过当前行，既第一行
            writer.passCurrentRow();

            //一次性写出内容
            writer.write(assets, true);

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
