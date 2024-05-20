package com.bbs.financial.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.financial.controller.SalaryController;
import com.bbs.financial.entity.Salary;
import com.bbs.financial.vo.SalaryVo;

/**
 *
 */
public interface SalaryService extends IService<Salary> {

    Page<SalaryVo> selectJoinList(SalaryController.SalaryListParam param);

    SalaryVo selectOneAndEmployeeSalary(Long id);


}
