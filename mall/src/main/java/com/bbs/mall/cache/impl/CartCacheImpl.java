package com.bbs.mall.cache.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.mall.cache.CartCache;
import com.bbs.mall.converter.CartConverter;
import com.bbs.mall.dto.CartDto;
import com.bbs.mall.dto.param.UpdCartParam;
import com.bbs.mall.entity.Cart;
import com.bbs.mall.enums.RedisKeys;
import com.bbs.mall.mq.RabbitmqConfig;
import com.bbs.mall.mq.RabbitmqSend;
import com.bbs.mall.service.CartService;
import com.bbs.mall.test.Constant;
import com.bbs.mall.util.RedisUtil;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartCacheImpl implements CartCache {
    @Resource
    private RedisUtil redis;

    @Resource
    private RabbitmqSend send;

    @Autowired
    private CartConverter cartConverter;

    @Autowired
    private CartService cartService;

    @Override
    public void create(CartDto dto, Long userId) {
        String key = RedisKeys.NEW_CART_USER.key() + userId;
        String hashKey = RedisKeys.NEW_CART_PRODUCT.key() + dto.getProdId();

        Cart toDB = cartConverter.toEntity(dto);
        toDB.setUserId(userId);

        //购物车商品缓存显示顺序
        Long count = redis.colSize(key);
        boolean isCache = count > 0 && (count <= Constant.CART_SIZE);
        if (isCache || (count == 0)) {//购物车商品存入缓存

            //TODO 改用Redission实现

            //存入缓存的购物车商品改用createTime排序
            dto.setSort(-1);
            toDB.setSort(-1);

            redis.hashSet(key, hashKey, JSON.toJSONString(dto));
            redis.colSet(key, dto.getProdId().toString());//存入当前商品id

            send.send(RabbitmqConfig.EXCHANGE_DIRECT_MALL, RabbitmqConfig.QUEUE_CART, JSON.toJSONString(toDB));
        } else {//购物车商品存入DB

            //TODO DB数据溢出小隐患
            //DB购物车商品顺序
            Integer sort = (Integer) redis.hashGet(key, RedisKeys.NEW_CART_DB_SORT.key());
            if (Objects.nonNull(sort)) {
                sort = sort + 1;
                toDB.setSort(sort);
                redis.hashSet(key, RedisKeys.NEW_CART_DB_SORT.key(), sort.toString());
            } else {
                toDB.setSort(1);
                redis.hashSet(key, RedisKeys.NEW_CART_DB_SORT.key(), "1");
            }

            cartService.save(toDB);
        }
    }

    @Override
    public List<CartDto> list(Long userId) {
        String key = RedisKeys.NEW_CART_USER.key() + userId;
        Set<String> tmpSet = redis.colGet(key);//缓存订单列表

        List<CartDto> dtos = tmpSet.stream()
                .map(p -> (String) redis.hashGet(key, RedisKeys.NEW_CART_PRODUCT + p))
                .filter(j -> !j.equals("-1"))//缓存删除可能不会及时，所以先把缓存数据设为-1作为双重保险机制
                .map(j -> JSON.parseObject(j, CartDto.class))
                .sorted((l, r) -> (int) (l.getCreateTime().getTime() - r.getCreateTime().getTime()))
                .collect(Collectors.toList());

        if (dtos.size() < 1) {//缓存里压根就没有数据
            return Collections.emptyList();
        }

        if (dtos.size() < Constant.CART_SIZE) {//双重保险机制，同tmpSet...filter()的原因
            int line = Constant.CART_SIZE - dtos.size();
            List<CartDto> dbDtos = new MPJLambdaWrapper<Cart>(Cart.class)
                    .select(Cart::getId, Cart::getProductBrand, Cart::getQuantity)
                    .select(Cart::getPrice, Cart::getProductName, Cart::getProductImg)
                    .select(Cart::getProductId, Cart::getSkuId, Cart::getCategoryId)
                    .select(Cart::getSort, Cart::getCreateTime, Cart::getUpdateTime)

                    .gt(Cart::getSort, -1)
                    .eq(Cart::getDeleteStatus, 1)
                    .eq(Cart::getUserId, userId)

                    .orderByAsc(Cart::getSort)
                    .page(new Page(1, line)).getRecords();

            dtos.addAll(dbDtos);
            return dtos;

            //TODO 从DB查出来的数据补到缓存中

        } else {
            return dtos;
        }
    }

    @Override
    public Result updateQuantity(Long userId, Long prodId, Integer quantity) {
        String key = RedisKeys.NEW_CART_USER.key() + userId;
        String hashKey = RedisKeys.NEW_CART_PRODUCT.key() + prodId;
        String json = (String) redis.hashGet(key, hashKey);

        if (!json.isEmpty()) {
            if (!json.equals("-1")) {//缓存删除可能不会及时，所以先把缓存数据设为-1作为双重保险机制
                CartDto dto = JSON.parseObject(json, CartDto.class);
                dto.setQuantity(quantity);
                dto.setUpdateTime(new Date());

                boolean isUpd = dto.getIsMQUpd();
                if (Objects.nonNull(isUpd) && !isUpd) {//MQ延时消息入库
                    Cart toDB = new Cart();
                    toDB.setUserId(userId);
                    toDB.setProductId(prodId);

                    send.send(RabbitmqConfig.EXCHANGE_DIRECT_MALL, RabbitmqConfig.QUEUE_CART_UPD_C, JSON.toJSONString(toDB));

                    dto.setIsMQUpd(true);
                }

                redis.hashSet(key, hashKey, JSON.toJSONString(dto));
                return Result.success();
            } else {
                //TDDO 打日志(缓存实际并没有删)
                return Result.failed("cache del later");
            }
        } else {//从DB取的数据暂直接改DB
            Cart toDB = new Cart();
            toDB.setUserId(userId);
            toDB.setProductId(prodId);
            toDB.setQuantity(quantity);
            toDB.setUpdateTime(new Date());
            return cartService.updateQuantity(toDB);
        }
    }

    @Override
    public Result updateAttr(UpdCartParam param, Long userId) {
        String key = RedisKeys.NEW_CART_USER.key() + userId;
        String hashKey = RedisKeys.NEW_CART_PRODUCT.key() + param.getProductId();
        String json = (String) redis.hashGet(key, hashKey);

        if (!json.isEmpty()) {
            if (!json.equals("-1")) {//缓存删除可能不会及时，所以先把缓存数据设为-1作为双重保险机制
                CartDto dto = JSON.parseObject(json, CartDto.class);
                dto.setPrice(param.getPrice());
                dto.setProdPic(param.getProductImg());
                dto.setSkuId(param.getSkuId());
                dto.setUpdateTime(param.getUpdateTime());

                //MQ延时消息入库
                boolean isUpd = dto.getIsMQUpd();
                if (Objects.nonNull(isUpd) && !isUpd) {
                    JSONObject toMQ = new JSONObject();
                    toMQ.put("userId", userId);
                    toMQ.put("prodId", dto.getProdId());

                    send.send(RabbitmqConfig.EXCHANGE_DIRECT_MALL, RabbitmqConfig.QUEUE_CART_UPD_C, toMQ.toJSONString());
                }

                dto.setIsMQUpd(true);
                redis.hashSet(key, hashKey, JSON.toJSONString(dto));
                return Result.success();
            } else {
                //TDDO 打日志(缓存实际并没有删)
                return Result.failed("cache del later");
            }
        } else {
            Long createTime = param.getCreateTime().getTime();
            Long updateTime = param.getUpdateTime().getTime();

            boolean isDone = false;
            Cart toDB = cartConverter.toEntity(param);
            if ((updateTime - createTime) < Constant.CART_DEL_TIME) {//商品添加到购物车后，短时间内改规格则直接改DB
                isDone = cartService.updateById(toDB);
            } else {//商品添加到购物车后，长时间后改规格则逻辑删除原规格，再添加新规格
                Cart tmp = cartService.getById(toDB.getId());//规避修改后用户秒删
                if (Objects.nonNull(tmp)) {

                    isDone = cartService.update()//逻辑删除原规格
                            .set("delete_status", 2)
                            .set("update_time", toDB.getUpdateTime())
                            .eq("id", toDB.getId())
                            .update();

                    if (isDone) {//添加新规格
                        tmp.setId(0L);
                        tmp.setPrice(toDB.getPrice());
                        tmp.setProductImg(toDB.getProductImg());
                        tmp.setSkuId(toDB.getSkuId());
                        tmp.setCreateTime(toDB.getUpdateTime());
                        tmp.setUpdateTime(toDB.getUpdateTime());
                        return cartService.create(tmp);
                    } else {
                        return Result.failed("upd fail");
                    }

                } else {
                    return Result.failed("cart's product del");
                }
            }

            if (isDone) {
                return Result.success();
            } else {
                return Result.failed("upd fail");
            }
        }
    }

    @Override
    public Result delete(Long userId, Long mergeId) {
        String key = RedisKeys.NEW_CART_USER.key() + userId;
        String hashKey = RedisKeys.NEW_CART_PRODUCT.key() + mergeId;

        Cart toDB = new Cart();
        toDB.setUserId(userId);
        toDB.setDeleteStatus(2);
        toDB.setUpdateTime(new Date());

        String json = (String) redis.hashGet(key, hashKey);
        if (Objects.nonNull(json)) {//先缓存删除，再DB逻辑删除

            //因为删除缓存数据可能会延时，所以需要提前设无效值，避免这种情况。
            redis.hashSet(key, hashKey, "-1");//TODO 当key、hashKey取出的数据是-1，说明数据已删除
            redis.hashDel(key, hashKey);
            redis.colDel(key, mergeId);//虽说同样会遇到key可能不会及时删，但由于上面的赋值，也只能获得无效值。

            toDB.setProductId(mergeId);
        } else {//DB直接逻辑删除
            toDB.setId(mergeId);
        }

        if (cartService.delete(toDB)) {
            return Result.success();
        } else {
            return Result.failed("db del fail");
        }
    }
}