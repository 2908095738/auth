package com.bbs.financial.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.entity.EmployeeItemExtend;
import com.bbs.financial.entity.EmployeeSalary;
import com.bbs.financial.entity.Salary;
import com.bbs.financial.enums.SalayExeclHeaderEnum;
import com.bbs.financial.service.EmployeeItemExtendService;
import com.bbs.financial.service.EmployeeSalaryService;
import com.bbs.financial.service.SalaryService;
import com.bbs.financial.service.SalaryVoucherItemService;
import com.bbs.financial.vo.SalaryVo;
import com.bbs.financial.vo.SalaryVoucherItemVo;
import com.bbs.vo.BaseParam;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.bbs.Result.success;

/**
 * 工资Controller
 * @author vctgo
 * @date 2024-05-15
 */
@RestController
@Slf4j
public class SalaryController {

    @Resource
    private SalaryService salaryService;

    @Resource
    private EmployeeSalaryService employeeSalaryService;

    @Resource
    private SalaryVoucherItemService salaryVoucherItemService;

    @Resource
    private EmployeeItemExtendService employeeItemExtendService;

    @Data
    public static class SalaryListParam extends BaseParam {

        /**
         * 导入日期
         */
        private String importDate;

        /**
         * 关联的工资类型
         */
        private Integer typeId;

        /**
         * 公司id
         */
        private Long companyId = 1L;

    }

    /**
     * 查询工资列表
     */
    @GetMapping("/salary/list")
    public Result<Page<SalaryVo>> list(SalaryListParam param)
    {
        return success(salaryService.selectJoinList(param));
    }

    /**
     * 获取工资详细信息
     */
    @GetMapping(value = "/salary/{id}")
    public Result<SalaryVo> getInfo(@PathVariable("id") Long id)
    {
        return success(salaryService.selectOneAndEmployeeSalary(id));
    }



    /**
     * 导入工资表
     */
    @Transactional
    @PostMapping("/salary/import")
    public Result<Boolean> add(@RequestParam("importDate") String importDate,@RequestParam("typeId") Long typeId,@RequestParam("file") MultipartFile file)
    {
        Long netAmountCount = 0L;
        List<EmployeeSalary> employeeSalaryArrayList = new ArrayList<>();
        List<EmployeeItemExtend> employeeItemExtends = new ArrayList<>();
        try {
            ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
            reader.setIgnoreEmptyRow(true);
            List<Map<String,Object>> list = reader.read(2,3, Integer.MAX_VALUE);
            netAmountCount = initEmployeeSalary(list,employeeSalaryArrayList,netAmountCount,employeeItemExtends);
            log.info("{}",list);
            Salary salary = new Salary().setCompanyId(1L).setImportDate(importDate).setTypeId(typeId).setNetAmount(netAmountCount).setStaffCount(list.size());
            salaryService.save(salary);
            employeeSalaryArrayList.forEach(employeeSalary -> employeeSalary.setSalaryId(salary.getId()));
            employeeSalaryService.saveBatch(employeeSalaryArrayList);
            employeeItemExtendService.saveBatch(employeeItemExtends);
        }   catch (Exception e) {
            log.error("导入失败",e);
            return Result.failed("导入失败");
        }
        return success();
    }

    /**
     * 初始化员工工资
     *
     * @param list
     * @param employeeSalaryArrayList
     * @param netAmountCount
     * @param employeeItemExtends
     */
    private Long initEmployeeSalary(List<Map<String, Object>> list, List<EmployeeSalary> employeeSalaryArrayList, Long netAmountCount, List<EmployeeItemExtend> employeeItemExtends) {
        Map<Integer, List<SalaryVoucherItemVo>> groupByType = salaryVoucherItemService.selectjoinByIsActive(1L, 1).stream().collect(Collectors.groupingBy(SalaryVoucherItemVo::getType));
        List<SalaryVoucherItemVo> salaryVoucherItemVoByEmployee = groupByType.get(1);
        List<SalaryVoucherItemVo> salaryVoucherItemVoBySalary = groupByType.get(0);

        for (int i = 0; i < list.size(); i++) {

            Map<String, Object> employeeSalaryMap = list.get(i);
            //如果员工的数据为空，则报错
            for (SalaryVoucherItemVo salaryVoucherItemVo : salaryVoucherItemVoByEmployee) {
                if (Objects.isNull(employeeSalaryMap.get(salaryVoucherItemVo.getTypeName()))){
                    throw new RuntimeException();
                }
            }
            String jobId = Objects.nonNull(employeeSalaryMap.get(SalayExeclHeaderEnum.ID.getValue())) ? employeeSalaryMap.get(SalayExeclHeaderEnum.ID.getValue()).toString() : null;//工号
            String name = Objects.nonNull(employeeSalaryMap.get(SalayExeclHeaderEnum.NAME.getValue())) ? employeeSalaryMap.get(SalayExeclHeaderEnum.NAME.getValue()).toString() : null;//名称
            String groupName = Objects.nonNull(employeeSalaryMap.get(SalayExeclHeaderEnum.DEPARTMENT.getValue())) ? employeeSalaryMap.get(SalayExeclHeaderEnum.NAME.getValue()).toString() : null;//部门
            String idCard = Objects.nonNull(employeeSalaryMap.get(SalayExeclHeaderEnum.ID_NUMBER.getValue()))?employeeSalaryMap.get(SalayExeclHeaderEnum.ID_NUMBER.getValue()).toString():null;//身份证号


            Double grossAmount = StringUtils.isNotEmpty(employeeSalaryMap.get(SalayExeclHeaderEnum.TOTAL_INCOME.getValue()).toString()) ?Double.parseDouble(employeeSalaryMap.get(SalayExeclHeaderEnum.TOTAL_INCOME.getValue()).toString())*100:0;//应发
            Double netAmount = StringUtils.isNotEmpty(employeeSalaryMap.get(SalayExeclHeaderEnum.NET_SALARY.getValue()).toString())?Double.parseDouble(employeeSalaryMap.get(SalayExeclHeaderEnum.NET_SALARY.getValue()).toString())*100:0;//实发

            EmployeeSalary employeeSalary = new EmployeeSalary();
            employeeSalary.setSalaryId(1L);
            employeeSalary.setEmployeeId(i+1L);
            employeeSalary.setEmployeeName(name);
            employeeSalary.setIdCard(idCard);
            employeeSalary.setJobCard(jobId);

            employeeSalary.setGrossAmount(grossAmount.longValue());
            employeeSalary.setNetAmount(netAmount.longValue());
            employeeSalaryArrayList.add(employeeSalary);

            for (SalaryVoucherItemVo salaryVoucherItemVo:salaryVoucherItemVoBySalary){
                Double fieldContent =  employeeSalaryMap.get(salaryVoucherItemVo.getTypeName())!=null&& StrUtil.isNotEmpty(employeeSalaryMap.get(salaryVoucherItemVo.getTypeName()).toString()) ?Double.parseDouble(employeeSalaryMap.get(salaryVoucherItemVo.getTypeName()).toString()):0;
                employeeItemExtends.add(new EmployeeItemExtend().setSalaryId(1L).setEmployeeId(i+1L).setContent(fieldContent.longValue()).setItemTypeId(salaryVoucherItemVo.getTypeId()));
            }

            netAmountCount=netAmountCount+netAmount.longValue();
        }
        return netAmountCount;
    }



    /**
     * 修改工资
     */
    @Transactional
    @PutMapping("/salary")
    public Result<Boolean> edit(@RequestBody Salary salary)
    {
        salaryService.updateById(salary);
        return success();
    }

    /**
     * 删除工资
     */
    @Transactional
    @DeleteMapping("/salary/{ids}")
    public Result<Boolean> remove(@PathVariable List<Long> ids)
    {
        salaryService.getBaseMapper().deleteBatchIds(ids);
        employeeSalaryService.remove(new QueryWrapper<EmployeeSalary>().in("salary_id",ids));
        employeeItemExtendService.remove(new QueryWrapper<EmployeeItemExtend>().in("salary_id",ids));
        return success();
    }
}