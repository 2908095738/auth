package com.clinic.controller;

import com.bbs.Result;
import com.clinic.app.AppStockService;
import com.clinic.dto.param.PutStock;
import com.clinic.dto.param.PutStockList;
import com.clinic.dto.param.StockSearchParam;
import com.clinic.enums.DrugStockRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@Slf4j
@RestController
@RequestMapping("/stock")
public class StockController {

    private AppStockService app;

    @GetMapping
    public Result search(StockSearchParam param) {
        return app.search(param);
    }

    @GetMapping("/role")
    public Result getWarnRole() {
        return Result.success(DrugStockRule.list);
    }

    /**
     * 药品入库
     */
    @PostMapping
    public Result<Boolean> putStock(@RequestBody @Valid PutStock param) {
        return app.putStock(param);
    }

    /**
     * 药品入库（批量）
     */
    @PutMapping("/list")
    public Result putStockList(@RequestBody @Valid PutStockList param) {
        return app.putStockList(param);
    }

    @Autowired
    public StockController(AppStockService app) {
        this.app = app;
    }
}
