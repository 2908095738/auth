package com.bbs.financial.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.entity.PriceType;
import com.bbs.financial.service.PriceTypeService;
import com.bbs.financial.util.LoginUser;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static com.bbs.Result.success;

/**
 * 币别控制器
 */
@RestController
@RequestMapping("/priceType")
public class PriceTypeController {
    @Resource
    private PriceTypeService priceTypeService;

    /**
     * 新增币别
     */
    @PostMapping
    public Result<Boolean> add(@RequestBody PriceType priceType) {
        priceType.setCompanyId(LoginUser.getCompanyId());
        priceTypeService.save(priceType);
        return success();
    }

    /**
     * 查询币别列表
     */
    @GetMapping("/list")
    public Result<Page<PriceType>> list(PriceType priceType, @RequestParam Integer current, @RequestParam Integer size) {
        return success(priceTypeService.page(new Page<>(current, size), new QueryWrapper<>(priceType)));
    }
}