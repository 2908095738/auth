package com.bbs.mall.controller;

import com.bbs.Result;
import com.bbs.mall.converter.ProductConverter;
import com.bbs.mall.dto.ProdDetailDto;
import com.bbs.mall.dto.ProductDto;
import com.bbs.mall.dto.param.ProductParam;
import com.bbs.mall.entity.Product;
import com.bbs.mall.service.ProductService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 商品管理
 */
@Controller
@RequestMapping("/product")
public class ProductController {

    private ProductConverter productConverter;

    private ProductService productService;

    @Autowired
    public ProductController(ProductConverter productConverter, ProductService productService) {
        this.productConverter = productConverter;
        this.productService = productService;
    }

    @ResponseBody
    @ApiOperation("获取商品详情")
    @GetMapping(value = "/getDetail")
    public Result<ProdDetailDto> getDetail(@ApiParam("商品id") Long prodId) {
        ProdDetailDto dto = productService.getDetail(prodId);
        if (Objects.nonNull(dto)) {
            return Result.success(dto);
        } else {
            return Result.failed("product no data");
        }
    }


}