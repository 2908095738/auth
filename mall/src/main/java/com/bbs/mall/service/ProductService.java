package com.bbs.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.Result;
import com.bbs.mall.bo.LowProductBO;
import com.bbs.mall.dto.CartLowDto;
import com.bbs.mall.dto.ProdDetailDto;
import com.bbs.mall.dto.ProductDto;
import com.bbs.mall.dto.param.ProductParam;
import com.bbs.mall.entity.Product;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;

public interface ProductService extends IService<Product> {


    /**
     * 获取商品简要信息
     *
     * @param productId 商品id
     * @return
     */
    ProductDto getProduct(Long productId);

    /**
     * 获取商品详情
     *
     * @param prodId 商品id
     * @return
     */
    ProdDetailDto getDetail(Long prodId);

    /**
     * 获取所有商品
     */
    List<ProductDto> getList();

    /**
     * 获取促销信息列表
     *
     * @param prodIds 商品id列表
     * @return
     */
    List<LowProductBO> getLowList(List<Long> prodIds);
}