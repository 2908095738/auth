package com.bbs.financial.api.asset;

import com.bbs.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

import static com.bbs.Result.success;

@RestController
@RequestMapping
public class SearchImportTemplateNames {

    @GetMapping("/asset/import/template/names")
    public Result<List<String>> searchNames() {
        return success(Collections.singletonList("template.xlsx"));
    }
}
