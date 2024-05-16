package com.bbs.financial.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.financial.entity.SalaryVoucherItem;
import com.bbs.financial.vo.SalaryVoucherItemVo;

import java.util.List;

/**
 *
 */
public interface SalaryVoucherItemService extends IService<SalaryVoucherItem> {

    Page<SalaryVoucherItemVo> selectjoinPage(Page salaryVoucherItemPage, Long cId, Integer type);
}
