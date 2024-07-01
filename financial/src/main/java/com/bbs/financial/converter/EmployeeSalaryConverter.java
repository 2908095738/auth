package com.bbs.financial.converter;

import com.bbs.financial.dto.SalaryTemplate;
import com.bbs.financial.entity.EmployeeSalary;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmployeeSalaryConverter {

    EmployeeSalary toEntity(SalaryTemplate employeeSalaryTemplate);
}
