package com.bbs.financial.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.financial.controller.SalaryController;
import com.bbs.financial.entity.AuxiliaryCalculation;
import com.bbs.financial.entity.Certificate;
import com.bbs.financial.entity.EmployeeItemExtend;
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

                .selectCollection(EmployeeSalary.class,SalaryVo::getEmployeeSalaries,
                        o->o.collection(EmployeeItemExtend.class,EmployeeSalary::getEmployeeItemExtends,
                                o1->o1.association(AuxiliaryCalculation.class, EmployeeItemExtend::getName,result->result.result(AuxiliaryCalculation::getName))))


                .leftJoin(EmployeeSalary.class,EmployeeSalary::getSalaryId,Salary::getId)

                .leftJoin(EmployeeItemExtend.class, on -> on
                    .eq(EmployeeItemExtend::getSalaryId,EmployeeSalary::getSalaryId)
                    .eq(EmployeeItemExtend::getEmployeeId,EmployeeSalary::getEmployeeId)
                )
                .leftJoin(AuxiliaryCalculation.class,on->on.eq(AuxiliaryCalculation::getId,EmployeeItemExtend::getItemTypeId))

                .selectAssociation("jc",Certificate.class,SalaryVo::getJCertificate)
                .selectAssociation("fc",Certificate.class,SalaryVo::getFCertificate)
                .leftJoin(Certificate.class,"jc",Certificate::getId,Salary::getJCertificateId)
                .leftJoin(Certificate.class,"fc",Certificate::getId,Salary::getFCertificateId)

                .eq(Salary::getIsDeleted,0)
                .eq(Objects.nonNull(param.getTypeId()),Salary::getTypeId,param.getTypeId())
                .eq(Objects.nonNull(param.getImportDate()),Salary::getImportDate,param.getImportDate())
                .eq(Salary::getCompanyId,param.getCompanyId())
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




