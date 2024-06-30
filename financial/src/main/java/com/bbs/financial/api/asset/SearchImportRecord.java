package com.bbs.financial.api.asset;

import com.bbs.Result;
import com.bbs.financial.entity.AssetImportRecord;
import com.bbs.financial.service.AssetImportRecordService;
import com.bbs.financial.util.LoginUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping
public class SearchImportRecord {

    @Resource
    private AssetImportRecordService recordService;

    @GetMapping("/asset/import/record")
    public Result<List<AssetImportRecord>> search() {
        return Result.success(recordService.lambdaQuery().eq(AssetImportRecord::getCompanyId, LoginUser.getCompanyId()).list());
    }
}
