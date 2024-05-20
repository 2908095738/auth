package com.bbs.financial.controller;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
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
        private Long cId = 1L;

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
            initEmployeeSalary(list,employeeSalaryArrayList,netAmountCount,employeeItemExtends);
            log.info("{}",list);
            Salary salary = new Salary().setCId(1L).setImportDate(importDate).setTypeId(typeId).setNetAmount(netAmountCount).setStaffCount(list.size());
            salaryService.save(salary);
            employeeSalaryArrayList.stream().forEach(employeeSalary -> {
                employeeSalary.setSalaryId(salary.getId());
            });
            employeeSalaryService.saveBatch(employeeSalaryArrayList);
            employeeItemExtendService.saveBatch(employeeItemExtends);
        }   catch (Exception e) {
            log.error("导入失败",e);
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
    private void initEmployeeSalary(List<Map<String, Object>> list, List<EmployeeSalary> employeeSalaryArrayList, Long netAmountCount, List<EmployeeItemExtend> employeeItemExtends) {
        Page<SalaryVoucherItemVo> salaryVoucherItemVoPage = salaryVoucherItemService.selectjoinPage(new Page<>(1, 999), 1L, 0);


        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> employeeSalaryMap = list.get(i);
//                employeeSalaryMap.get(SalayExeclHeaderEnum.ID.getValue());//工号
//                employeeSalaryMap.get(SalayExeclHeaderEnum.NAME.getValue());//姓名
//                employeeSalaryMap.get(SalayExeclHeaderEnum.NAME.getValue());//部门
//                employeeSalaryMap.get(SalayExeclHeaderEnum.ID_NUMBER.getValue());//身份证号码
            Double grossAmount = StringUtils.isNotEmpty(employeeSalaryMap.get(SalayExeclHeaderEnum.TOTAL_INCOME.getValue()).toString()) ?Double.parseDouble(employeeSalaryMap.get(SalayExeclHeaderEnum.TOTAL_INCOME.getValue()).toString()):0;//应发
            Double netAmount = StringUtils.isNotEmpty(employeeSalaryMap.get(SalayExeclHeaderEnum.NET_SALARY.getValue()).toString())?Double.parseDouble(employeeSalaryMap.get(SalayExeclHeaderEnum.NET_SALARY.getValue()).toString()):0;//实发


            EmployeeSalary employeeSalary = new EmployeeSalary();
            employeeSalary.setSalaryId(1L);
            employeeSalary.setEmployeeId(i+1L);
            employeeSalary.setGrossAmount(grossAmount.longValue());
            employeeSalary.setNetAmount(netAmount.longValue());
            employeeSalaryArrayList.add(employeeSalary);

            for (SalaryVoucherItemVo salaryVoucherItemVo:salaryVoucherItemVoPage.getRecords()){
                Double fieldContent =  employeeSalaryMap.get(salaryVoucherItemVo.getTypeName())!=null?Double.parseDouble(employeeSalaryMap.get(salaryVoucherItemVo.getTypeName()).toString()):0;
                employeeItemExtends.add(new EmployeeItemExtend().setSalaryId(1L).setEmployeeId(i+1L).setContent(fieldContent.longValue()).setItemTypeId(salaryVoucherItemVo.getTypeId()));
            }
            netAmountCount+=netAmount.longValue();
        }

    }



    /**
     * 修改工资
     */
    @PutMapping("/salary")
    public Result<Boolean> edit(@RequestBody Salary salary)
    {
        salaryService.updateById(salary);
        return success();
    }

    /**
     * 删除工资
     */
    @DeleteMapping("/salary/{ids}")
    public Result<Boolean> remove(@PathVariable List<Long> ids)
    {
        salaryService.getBaseMapper().deleteBatchIds(ids);
        return success();
    }
}