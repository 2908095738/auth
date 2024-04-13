package com.bbs.mall.controller;

import com.bbs.Result;
import com.bbs.mall.cache.CartCache;
import com.bbs.mall.converter.CartConverter;
import com.bbs.mall.dto.CartDto;
import com.bbs.mall.dto.CartLowDto;
import com.bbs.mall.dto.ProductDto;
import com.bbs.mall.dto.param.CartParam;
import com.bbs.mall.dto.param.UpdCartParam;
import com.bbs.mall.service.CartService;
import com.bbs.mall.service.ProductService;
import com.bbs.mall.util.RedisUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

//TODO 分页的size要和Constant.CART_SIZE一致

/**
 * 购物车管理
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    private CartConverter cartConverter;

    private CartCache cartCache;

    private ProductService productService;

    private CartService cartService;

    @Autowired
    public CartController(CartConverter cartConverter, CartCache cartCache, ProductService productService, CartService cartService) {
        this.cartConverter = cartConverter;
        this.cartCache = cartCache;
        this.productService = productService;
        this.cartService = cartService;
    }

    @Resource
    private RedisUtil redis;

    //TODO redisTest
    @ResponseBody
    @GetMapping("/test")
    public Result test(int type) {
        switch (type) {
            case 1:
                redis.colSet("111", "vvv");
                return Result.success("type->1：" + redis.colSize("111"));
            case 2:
                redis.colDel("111", "vvv");
                return Result.success("type->2：" + redis.colSize("111"));
            case 3:
                return Result.success("type->3：" + redis.colSize("111"));
            case 4:
                return Result.success("type->4：" + redis.hashGet("2k", "3hk"));
        }
        return Result.failed("type fail");
    }

    @ResponseBody
    @PutMapping("/create")
    public Result create(@RequestBody @Valid CartParam param) {
        CartDto dto = cartConverter.toDto(param);
        Date now = new Date();
        dto.setCreateTime(now);
        dto.setUpdateTime(now);

        //TODO 用户相关
//        AuthUtil.UserAPI.User currentUser = ThreadLocalUtil.getCurrentUser();
        Long userId = 1L;

        cartCache.create(dto, userId);
        return Result.success();
    }

    /**
     * 获取商品规格
     *
     * @param productId 商品id
     * @return
     */
    @ResponseBody
    @ApiOperation("获取商品规格")
    @GetMapping(value = "/getProduct")
    public Result<ProductDto> getProduct(Long productId) {
        ProductDto result = productService.getProduct(productId);

        if (Objects.nonNull(result)) {
            return Result.success(result);
        } else {
            return Result.failed("db no data");
        }
    }

    @ResponseBody
    @ApiOperation("分页获取购物车商品")
    @GetMapping(value = "/list")
    public Result<List<CartDto>> list(@ApiParam("页码；1：从缓存获取；>1：从DB获取") Integer current, @ApiParam("条数") Integer size) {
        if (current < 1) {
            return Result.failed("current err");
        }

        //TODO 用户相关
        //UserVO currentUser = ThreadLocalUtil.getCurrentUser();
        Long userId = 1L;

        List<CartDto> result;
        switch (current) {
            case 1:
                result = cartCache.list(userId);
                break;
            default:
                result = cartService.list(userId, current, size);
                break;
        }
        return Result.success(result);
    }

    //TODO 不可用，数据源有点问题需改动

    /**
     * 获取购物车中所有商品的促销信息
     *
     * @param prodIds 购物车商品id列表
     * @return
     */
    @ResponseBody
    @ApiOperation("获取购物车中所有商品的促销信息")
    @GetMapping(value = "/listLow")
    public Result<List<CartLowDto>> listLow(List<Long> prodIds) {
        //TODO 用户相关
        //UserVO currentUser = ThreadLocalUtil.getCurrentUser();
        Long userId = 1L;

        //获取购物车商品信息
        List<CartDto> dtoList = cartCache.list(userId);
        if (dtoList.isEmpty()) {
            return Result.failed("cart no data");
        }
        dtoList = dtoList.stream().filter(c -> prodIds.contains(c.getProdId())).collect(Collectors.toList());

        //获取购物粗优惠商品信息
        List<CartLowDto> result = cartService.listLow(dtoList);
        if (!result.isEmpty()) {
            return Result.success(result);
        } else {
            return Result.failed("db no data");
        }
    }

    //TODO Func：按分类获取购物车中的商品

    /**
     * TODO update系列Func更改思路：
     *  -   用户可能只是改着玩，所以不能直接改DB。
     *  -   如果是缓存数据直接改缓存，再发送简易MQ延时消息，到达后再从redis取数据去改DB数据。(Done)
     *  -   如果是DB数据临时加到缓存，同样的流程，不过数量有限，超过的还是直接改DB数据。(SOON)
     */

    @ResponseBody
    @ApiOperation("修改指定购物车商品的数量")
    @GetMapping(value = "/update/quantity")
    public Result updateQuantity(@ApiParam("商品id") Long prodId, @ApiParam("数量") Integer quantity) {
        //TODO 用户相关
        //UserVO currentUser = ThreadLocalUtil.getCurrentUser();
        Long userId = 1L;

        return cartCache.updateQuantity(userId, prodId, quantity);
    }

    @ResponseBody
    @ApiOperation("修改指定购物车商品的规格(子页面不提供改数量)")
    @PostMapping("/update/attr")
    public Result updateAttr(@RequestBody @Valid UpdCartParam param) {
        //TODO 用户相关
        //UserVO currentUser = ThreadLocalUtil.getCurrentUser();
        Long userId = 1L;

        return cartCache.updateAttr(param, userId);
    }

    @ResponseBody
    @ApiOperation("删除购物车商品")
    @DeleteMapping("/delete")
    public Result delete(@ApiParam("prodId/cartId；prodId：从缓存查询的数据；cartId：从DB查询的数据") Long mergeId) {
        //TODO 用户相关
//        AuthUtil.UserAPI.User currentUser = ThreadLocalUtil.getCurrentUser();
        Long userId = 1L;

        return cartCache.delete(userId, mergeId);
    }
}