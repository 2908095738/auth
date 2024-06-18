package com.bbs.mall.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.mall.bo.LowProductBO;
import com.bbs.mall.dto.ProdDetailDto;
import com.bbs.mall.dto.ProdDetallDto;
import com.bbs.mall.dto.ProdDto;
import com.bbs.mall.dto.ProductDto;
import com.bbs.mall.entity.*;
import com.bbs.mall.mapper.ProdAttrValueMapper;
import com.bbs.mall.mapper.ProdPicMapper;
import com.bbs.mall.mapper.ProductMapper;
import com.bbs.mall.service.ProductService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl extends MPJBaseServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private ProdAttrValueMapper valueMapper;

    @Autowired
    private ProdPicMapper picMapper;

    @Override
    public Page<ProdDto> list(Integer current, Integer size) {
        return new MPJLambdaWrapper<ProdDto>()
                .selectAs(Product::getMainPic, ProdDto::getProdPic)
                .selectAs(Product::getName, ProdDto::getProdName)
                .select(Product::getId, Product::getPrice, Product::getBrandLogo)
                .select(Product::getBrandName, Product::getBrandId)
                .page(new Page(current, size));
    }

    @Override
    public ProdDetallDto getDetail(Long prodId) {
        //获取商品详情DTO
        ProdDetallDto dto = new MPJLambdaWrapper<ProdDetallDto>()
                .select(Product::getBrandLogo, Product::getBrandName, Product::getPrice)
                .select(Product::getDescription)

                .eq(Product::getId, prodId)
                .one();
        if (Objects.isNull(dto)) {
            return null;
        }

        //DTO赋值
        dto.setId(prodId);

        //获取商品属性列表
        MPJLambdaWrapper valueWrap = new MPJLambdaWrapper<ProdDetallDto.AttrValueDto>()
                .select(ProdAttr::getName)
                .select(ProdAttrValue::getValue)
                .leftJoin(ProdAttr.class, ProdAttr::getId, ProdAttrValue::getProdAttrId)
                .eq(ProdAttrValue::getProdId, prodId);
        List<ProdDetallDto.AttrValueDto> values = valueMapper.selectJoinList(ProdDetallDto.AttrValueDto.class, valueWrap);
        if (Objects.nonNull(values) && !values.isEmpty()) {
            dto.setValues(values);
        }

        //获取商品图片列表
        MPJLambdaWrapper picWrap = new MPJLambdaWrapper<String>()
                .select(ProdPic::getThePic)
                .eq(ProdPic::getProdId, prodId)
                .orderByAsc(ProdPic::getSort);
        List<String> pics = picMapper.selectJoinList(String.class, picWrap);
        if (Objects.nonNull(pics) && !pics.isEmpty()) {
            dto.setPics(pics);
        }

        return dto;
    }

    //TODO 原始版本获取详情
    private ProdDetailDto oriGetDetail(Long prodId) {
        ProdDetailDto dto = new ProdDetailDto();

        //TODO 用mybatis把简易多次查询串成一个

//        //商品信息赋值
//        Product prod = getOptById(prodId).orElse(null);
//        if (Objects.nonNull(prod)) {
//            dto.setProduct(prod);
//        } else {
//            return null;
//        }
//
//        //商品品牌赋值
//        Brand brand = new MPJLambdaWrapper<Brand>(Brand.class)
//                .selectAll(Brand.class)
//                .eq(Brand::getId, prod.getBrandId())
//                .one();
//        if (Objects.nonNull(brand)) {
//            dto.setBrand(brand);
//        } else {
//            return null;
//        }
//
//        //商品属性信息(key)赋值
//        List<ProdAttr> attrs = new MPJLambdaWrapper<ProdAttr>(ProdAttr.class)
//                .selectAll(ProdAttr.class)
////                .eq(ProdAttr::getProdId, prodId)
//                .list();
//        dto.setAttrs(attrs);
//
//        //商品属性值(value)赋值
//        List<Long> attrIds = attrs.stream().map(ProdAttr::getId).collect(Collectors.toList());
//        List<ProdAttrValue> attrValues = new MPJLambdaWrapper<ProdAttrValue>(ProdAttrValue.class)
//                .selectAll(ProdAttrValue.class)
//                .eq(ProdAttrValue::getProdId, prodId)
//                .in(ProdAttrValue::getProdAttrId, attrIds)
//                .list();
//        dto.setAttrValues(attrValues);
//
//        //商品SKU库存赋值
//        List<Sku> skus = new MPJLambdaWrapper<Sku>(Sku.class)
//                .selectAll(Sku.class)
//                .eq(Sku::getProductId, prodId)
//                .list();
//        dto.setSkus(skus);
//
//        //商品促销规则赋值
//        List<String> jsons = new ArrayList();
//        switch (prod.getLowType()) {
//            case 3:
//                jsons = new MPJLambdaWrapper<ProdLadder>(ProdLadder.class)
//                        .selectAll(ProdLadder.class)
//                        .eq(ProdLadder::getProdId, prodId)
//                        .list()
//                        .stream()
//                        .map(l -> JSON.toJSONString(l))
//                        .collect(Collectors.toList());
//                break;
//            case 4:
//                jsons = new MPJLambdaWrapper<ProdFullReduce>(ProdFullReduce.class)
//                        .selectAll(ProdFullReduce.class)
//                        .eq(ProdFullReduce::getProdId, prodId)
//                        .list()
//                        .stream()
//                        .map(r -> JSON.toJSONString(r))
//                        .collect(Collectors.toList());
//                break;
//        }
//        dto.setLowRuleJson(jsons);

        //TODO 商品详情页优惠券
        return dto;
    }

    @Override
    public List<ProductDto> getList() {
        return null;
    }

    @Override
    public List<LowProductBO> getLowList(List<Long> prodIds) {
        //
        /**
         *  TODO 暂时先以这样的方式实现，后续琢磨琢磨MybatisXXX改进
         *      -   现成的mapper一次查询实现在mall->PortalProductDao->getPromotionProductList
         */

        List<LowProductBO> result = new ArrayList();
        for (Long nowId : prodIds) {
            //BO本体赋值
            LowProductBO theBO = new LowProductBO();
            //TODO 字段被移除，防报错故删掉
//            Integer nowType = new MPJLambdaWrapper<LowProductBO>()
//                    .select(Product::getLowType)
//                    .eq(Product::getId, nowId)
//                    .one(Integer.class);
//
//            theBO.setId(nowId);
//            theBO.setLowType(nowType);

            //促销SKU赋值
            List<LowProductBO.LowSkuBO> skus = new MPJLambdaWrapper<LowProductBO.LowSkuBO>()
                    .select(Sku::getSkuCode, Sku::getPrice, Sku::getLowPrice)
                    .select(Sku::getStock, Sku::getLockStock)
                    .selectAs(Sku::getId, LowProductBO.LowSkuBO::getSkuId)

                    .leftJoin(Sku.class, Sku::getProductId, Product::getId)
                    .eq(Product::getId, nowId)
                    .list();

            theBO.setSkus(skus);

            //促销商品阶梯价格赋值
            List<LowProductBO.LowLadderBO> ladders = new MPJLambdaWrapper<LowProductBO.LowLadderBO>()
                    .select(ProdLadder::getCount, ProdLadder::getDiscount)
                    .selectAs(ProdLadder::getId, LowProductBO.LowLadderBO::getLadderId)
                    .leftJoin(ProdLadder.class, ProdLadder::getProdId, Product::getId)
                    .eq(Product::getId, nowId)
                    .list();

            theBO.setLadders(ladders);

            //促销商品满减价格赋值
            List<LowProductBO.LowReduceBO> reduces = new MPJLambdaWrapper<LowProductBO.LowReduceBO>()
                    .select(ProdFullReduce::getFullPrice, ProdFullReduce::getReducePrice)
                    .selectAs(ProdFullReduce::getId, LowProductBO.LowReduceBO::getReduceId)
                    .leftJoin(ProdFullReduce.class, ProdFullReduce::getProdId, Product::getId)
                    .eq(Product::getId, nowId)
                    .list();

            theBO.setReduces(reduces);

            result.add(theBO);
        }

        return result;
    }
}