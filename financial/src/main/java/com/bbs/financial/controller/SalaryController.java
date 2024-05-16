package com.bbs.financial.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.entity.Salary;
import com.bbs.financial.service.EmployeeSalaryService;
import com.bbs.financial.service.SalaryService;
import com.bbs.financial.vo.SalaryVo;
import com.bbs.vo.BaseParam;
import lombok.Data;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static com.bbs.Result.success;

/**
 * 工资Controller
 * @author vctgo
 * @date 2024-05-15
 */
@RestController
@RequestMapping("")
public class SalaryController {

    @Resource
    private SalaryService salaryService;

    @Resource
    private EmployeeSalaryService employeeSalaryService;

    @Data
    public static class SalaryListParam extends BaseParam {

        /**
         * 导入日期
         */
        private Date importDate;

        /**
         * 关联的工资类型
         */
        private Integer typeId;

    }

    /**
     * 查询工资列表
     */
    @GetMapping("/salary/list")
    public Result<Page<SalaryVo>> list(SalaryListParam param)
    {
//         new Page<>(SalaryListParam.getCurrent(), SalaryListParam.getSize()
        Page<SalaryVo> result = salaryService.selectJoinList(param);
        return success(null);
    }

    /**
     * 获取工资详细信息
     */
    @GetMapping(value = "/salary/{id}")
    public Result<Salary> getInfo(@PathVariable("id") Long id)
    {
        return success(salaryService.getById(id));
    }

    /**
     * 新增工资
     */
    @PostMapping("/salary")
    public Result<Boolean> add(@RequestBody Salary salary)
    {
        salaryService.save(salary);
        return success();
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