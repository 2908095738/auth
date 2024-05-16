package com.bbs.financial.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.financial.controller.SalaryController;
import com.bbs.financial.entity.Salary;
import com.bbs.financial.service.SalaryService;
import com.bbs.financial.mapper.SalaryMapper;
import com.bbs.financial.vo.SalaryVo;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class SalaryServiceImpl extends ServiceImpl<SalaryMapper, Salary>
    implements SalaryService{

    @Override
    public Page<SalaryVo> selectJoinList(SalaryController.SalaryListParam param) {
        return null;
    }
}




