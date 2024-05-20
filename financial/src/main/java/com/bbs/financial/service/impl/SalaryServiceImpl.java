package com.bbs.financial.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.financial.controller.SalaryController;
import com.bbs.financial.entity.Certificate;
import com.bbs.financial.entity.EmployeeSalary;
import com.bbs.financial.entity.Salary;
import com.bbs.financial.mapper.SalaryMapper;
import com.bbs.financial.service.SalaryService;
import com.bbs.financial.vo.SalaryVo;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 *
 */
@Slf4j
@Service
public class SalaryServiceImpl extends MPJBaseServiceImpl<SalaryMapper, Salary>
    implements SalaryService{

    @Override
    public Page<SalaryVo> selectJoinList(SalaryController.SalaryListParam param) {

        return selectJoinListPage(new Page<>(param.getCurrent(),param.getSize()),SalaryVo.class,new MPJLambdaWrapper<Salary>()
                .selectAll(Salary.class)
                .selectCollection(EmployeeSalary.class,SalaryVo::getEmployeeSalaries)
                .leftJoin(EmployeeSalary.class,EmployeeSalary::getSalaryId,Salary::getId)
                .selectAssociation(Certificate.class,SalaryVo::getJCertificate)
                .selectAssociation(Certificate.class,SalaryVo::getFCertificate)
                .leftJoin(Certificate.class,Certificate::getId,Salary::getJCertificateId)
                .leftJoin(Certificate.class,Certificate::getId,Salary::getFCertificateId)
                .eq(Salary::getIsDeleted,0)
                .eq(Objects.nonNull(param.getTypeId()),Salary::getTypeId,param.getTypeId())
                .eq(Objects.nonNull(param.getImportDate()),Salary::getImportDate,param.getImportDate())
                .eq(Salary::getCId,param.getCId())
                .orderBy(true,true,Salary::getImportDate)
        );
    }

    @Override
    public SalaryVo selectOneAndEmployeeSalary(Long id) {
        return selectJoinOne(SalaryVo.class,new MPJLambdaWrapper<Salary>()
                .selectAll(Salary.class)
                .selectCollection(EmployeeSalary.class,SalaryVo::getEmployeeSalaries)
                .leftJoin(EmployeeSalary.class,EmployeeSalary::getSalaryId,Salary::getId)
                .eq(Salary::getId,id)
        );
    }


}




