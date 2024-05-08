package com.manage.controller;

import com.manage.service.CustomizeTableService;
import com.manage.service.TableValueService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 管理后台 - 库存
 */
@RestController
@RequestMapping("/stock")
public class StockController {
    @Resource
    private CustomizeTableService customizeTableService;
    @Resource
    private TableValueService tableValueService;


    //库存




}
