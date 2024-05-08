package com.bbs.mall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.Result;
import com.bbs.mall.bo.LowProductBO;
import com.bbs.mall.dto.*;
import com.bbs.mall.dto.param.ProductParam;
import com.bbs.mall.entity.Product;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;

public interface ProductService extends IService<Product> {

    /**
     * 分页获取商品
     *
     * @param current 页码
     * @param size    条数
     * @return
     */
    Page<ProdDto> list(Integer current, Integer size);

    /**
     * 获取商品详情
     *
     * @param prodId 商品id
     * @return
     */
    ProdDetallDto getDetail(Long prodId);

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