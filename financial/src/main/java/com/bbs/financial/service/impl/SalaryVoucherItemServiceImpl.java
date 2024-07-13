package com.bbs.financial.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.financial.entity.SalaryCalculate;
import com.bbs.financial.entity.SalaryVoucherItem;
import com.bbs.financial.mapper.SalaryVoucherItemMapper;
import com.bbs.financial.service.SalaryVoucherItemService;
import com.bbs.financial.vo.SalaryVoucherItemVo;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 *
 */
@Service
public class SalaryVoucherItemServiceImpl extends MPJBaseServiceImpl<SalaryVoucherItemMapper, SalaryVoucherItem>
    implements SalaryVoucherItemService{

    @Override
    public Page<SalaryVoucherItemVo> selectjoinPage(Page salaryVoucherItemPage, Long cId,Integer type) {
        return selectJoinListPage(salaryVoucherItemPage, SalaryVoucherItemVo.class,new MPJLambdaWrapper<SalaryVoucherItem>()
                .selectAll(SalaryVoucherItem.class)
                .leftJoin(SalaryCalculate.class, SalaryCalculate::getId, SalaryVoucherItem::getAccountingItemTypeId, o-> o
                        .selectAs(SalaryCalculate::getName,SalaryVoucherItemVo::getTypeName)
                        .selectAs(SalaryCalculate::getId,SalaryVoucherItemVo::getTypeId))
                .eq(SalaryVoucherItem::getCompanyId, cId)
                .eq(Objects.nonNull(type),SalaryVoucherItem::getType, type)
        );
    }

    @Override
    public List<SalaryVoucherItemVo> selectjoinByIsActive( Long cId, Integer isActive) {
        return selectJoinList(SalaryVoucherItemVo.class,new MPJLambdaWrapper<SalaryVoucherItem>()
                .selectAll(SalaryVoucherItem.class)
                .leftJoin(SalaryCalculate.class, SalaryCalculate::getId, SalaryVoucherItem::getAccountingItemTypeId, o-> o
                        .selectAs(SalaryCalculate::getName,SalaryVoucherItemVo::getTypeName)
                        .selectAs(SalaryCalculate::getId,SalaryVoucherItemVo::getTypeId))
                .eq(SalaryVoucherItem::getCompanyId, cId)
                .eq(SalaryVoucherItem::getIsActive, isActive)
                .orderByDesc(SalaryVoucherItem::getCreatedAt)
        );
    }
}




