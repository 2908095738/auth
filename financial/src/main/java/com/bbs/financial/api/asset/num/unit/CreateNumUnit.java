package com.bbs.financial.api.asset.num.unit;

import com.bbs.Result;
import com.bbs.financial.entity.AssetNumUnit;
import com.bbs.financial.service.AssetNumUnitService;
import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping
public class CreateNumUnit {


    @Resource
    private AssetNumUnitService db;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {
        private String name;
    }

    @PutMapping("/asset/num/unit")
    public Result<List<AssetNumUnit>> create(@RequestBody Param param) throws IllegalArgumentException {
        String name = param.name;
        Preconditions.checkArgument(isEmpty(name), "单位已存在，无需重复创建");
        db.save(new AssetNumUnit(name));
        return Result.success(db.list());
    }

    private boolean isEmpty(String name) {
        return !db.lambdaQuery().eq(AssetNumUnit::getName, name).exists();
    }
}
