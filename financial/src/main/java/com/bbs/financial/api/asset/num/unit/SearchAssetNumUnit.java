package com.bbs.financial.api.asset.num.unit;

import com.bbs.Result;
import com.bbs.financial.entity.AssetNumUnit;
import com.bbs.financial.service.AssetNumUnitService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

import static org.apache.commons.lang3.math.NumberUtils.LONG_ZERO;

@RestController
@RequestMapping
public class SearchAssetNumUnit {

    @Resource
    private AssetNumUnitService db;

    @GetMapping("/asset/num/unit/list")
    public Result<List<AssetNumUnit>> list(@RequestParam Long companyId, @RequestParam(required = false) String name) {
        return Result.success(db.lambdaQuery()
                        .eq(AssetNumUnit::getCompanyId, LONG_ZERO)
                        .or()
                        .eq(AssetNumUnit::getCompanyId, companyId)
                        .like(StringUtils.isNotBlank(name), AssetNumUnit::getName, name)
        .list());
    }
}
