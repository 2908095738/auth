package com.bbs.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.mall.dto.ProdDetallDto;
import com.bbs.mall.dto.ProdDto;
import com.bbs.mall.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 商品管理
 */
@Controller
@Api(tags = "商品管理")
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @ResponseBody
    @ApiOperation("分页获取商品")
    @GetMapping(value = "/list")
    public Result<Page<ProdDto>> list(
            @ApiParam(value = "页码", name = "current", required = true) Integer current,
            @ApiParam(value = "条数", name = "size", required = true) Integer size) {
        return Result.success(productService.list(current, size));
    }

    @ResponseBody
    @ApiOperation("获取商品详情")
    @GetMapping(value = "/getDetail")
    public Result<ProdDetallDto> getDetail(@ApiParam(value = "商品id", name = "prodId", required = true) Long prodId) {
        ProdDetallDto dto = productService.getDetail(prodId);
        if (Objects.nonNull(dto)) {
            return Result.success(dto);
        } else {
            return Result.failed("product no data");
        }
    }
}