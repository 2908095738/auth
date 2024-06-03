package com.bbs.financial.api.asset;

import cn.hutool.core.io.FileUtil;
import com.bbs.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.bbs.Result.success;

@RestController
@RequestMapping
public class SearchImportTemplateNames {

    @Value("${asset.import.template.path}")
    private String templatePath;

    @GetMapping("/asset/import/template/names")
    public Result<List<String>> searchNames() {
        return success(Arrays.stream(FileUtil.ls(templatePath)).map(File::getName).collect(Collectors.toList()));
    }
}
