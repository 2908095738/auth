package com.bbs.financial.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.entity.PriceType;
import com.bbs.financial.entity.ZhangHu;
import com.bbs.financial.service.ZhangHuService;
import com.bbs.financial.util.LoginUser;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import java.util.*;
import java.util.stream.Collectors;

import static com.bbs.Result.success;

/**
 * 账户控制器
 */
@RestController
@RequestMapping("/zhanghu")
public class ZhangHuController {

    @Resource
    private ZhangHuService zhangHuService;

    /**
     * 新增账户
     */
    @PostMapping
    public Result<Boolean> add(@RequestBody ZhangHu zhanghu) {
        zhanghu.setAccountingSetId(LoginUser.getLoginSetId());
        zhangHuService.save(zhanghu);
        return success();
    }

    /**
     * 查询账户列表
     */
    @GetMapping("/list")
    public Result<Page<ZhangHu>> list(ZhangHu zhanghu, @RequestParam Integer current, @RequestParam Integer size) {
        //TODO L [导入日记账]暂时会导致重复新建同名账户，故通过Set筛选
        Page<ZhangHu> zhangHuPage = zhangHuService.selectJoinListPage(new Page<>(current, size), ZhangHu.class, new MPJLambdaWrapper<>(zhanghu)
                .selectAll(ZhangHu.class)
                .leftJoin(PriceType.class, PriceType::getId, ZhangHu::getMTypeId, ext -> ext.selectAssociation(PriceType.class, ZhangHu::getPriceType))
        );
        List<ZhangHu> tmpList = zhangHuPage.getRecords();

        List<ZhangHu> doneList = tmpList.stream().collect(
                Collectors.collectingAndThen(
                        Collectors.toCollection(
                                () -> new TreeSet<>(Comparator.comparing(ZhangHu::getName))), ArrayList::new)
        );
        zhangHuPage.setRecords(doneList);

        return success(zhangHuPage);
    }

    /**
     * 获取账户详细信息
     */
    @GetMapping(value = "/{id}")
    public Result<ZhangHu> getInfo(@PathVariable("id") Long id) {
        return success(zhangHuService.getById(id));
    }

    /**
     * 修改账户
     */
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody ZhangHu zhanghu) {
        zhangHuService.updateById(zhanghu);
        return success();
    }

    /**
     * 删除账户
     */
    @DeleteMapping("/{ids}")
    public Result<Boolean> remove(@PathVariable List<Long> ids) {
        zhangHuService.getBaseMapper().deleteBatchIds(ids);
        return success();
    }
}