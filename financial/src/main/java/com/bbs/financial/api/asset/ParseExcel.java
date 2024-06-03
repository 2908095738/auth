package com.bbs.financial.api.asset;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.bbs.Result;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;

@RestController
@RequestMapping
public class ParseExcel {

    private static final Set<String> ASSET_FIELD_SET = new HashSet<>(Arrays.asList(
            "资产编码",
            "资产名称",
            "资产类别",
            "部门",
            "使用人",
            "开始使用日期",
            "数量",
            "数量单位",
            "规格型号",
            "存放地点",
            "折旧方法",
            "使用月数",
            "原值",
            "税额",
            "残值率",
            "预计残值",
            "减值准备",
            "已折旧月数",
            "期初净值",
            "期初累计折旧",
            "平均月折旧额",
            "当月折旧额",
            "本年折旧额",
            "期末累计折旧",
            "期末净值",
            "期末减值准备",
            "清理月份",
            "状态",
            "备注",
            "创建时间",
            "信息创建人",
            "修改时间",
            "信息修改人"
    ));

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Column {

        private String source;

        private String target;

        public Column(String target) {
            this.target = target;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VO {

        private Set<String> columns;

        private List<Column> mapping;
    }

    @PostMapping("/asset/file/parse")
    public Result<VO> upload(
            @RequestParam MultipartFile file
    ) throws IOException {
        // 解析 excel
        ExcelReader excel = ExcelUtil.getReader(file.getInputStream());
        // 获取第一行（列名）
        Set<String> columns = excel.readRow(INTEGER_ZERO).stream().map(Object::toString).collect(Collectors.toSet());
        // 转换为 Column(Object) 如果 excel 中列名和实体类属性名不一致，则 source 为空
        List<Column> mapping = ASSET_FIELD_SET.stream().map(targetColumn -> {
            Column column = new Column(targetColumn);
            if (columns.contains(targetColumn)) column.setSource(targetColumn);
            return column;
        }).collect(Collectors.toList());

        return Result.success(new VO(columns, mapping));
    }
}
