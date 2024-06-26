package com.bbs.financial.controller;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.api.auth.User;
import com.bbs.api.auth.UserAPI;
import com.bbs.api.auth.company.CompanyAPI;
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
import com.bbs.vo.CompanyStructure;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
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
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

    @DubboReference
    private CompanyAPI companyAPI;

    @DubboReference
    private UserAPI userAPI;

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
        private Long companyId;

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
    public Result<Boolean> add(@RequestParam("companyId") Long companyId,@RequestParam("importDate") String importDate,@RequestParam("typeId") Long typeId,@RequestParam("file") MultipartFile file){
        Long netAmountCount = 0L;
        List<EmployeeSalary> employeeSalaryArrayList = new ArrayList<>();
        List<EmployeeItemExtend> employeeItemExtends = new ArrayList<>();


        try {
            ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
            reader.setIgnoreEmptyRow(true);
            List<Map<String,Object>> list = reader.read(2,3, Integer.MAX_VALUE);
            if(CollectionUtils.isNotEmpty(list)){
                Map<Integer, List<SalaryVoucherItemVo>> groupByType = salaryVoucherItemService.selectjoinByIsActive(companyId, 1).stream().collect(Collectors.groupingBy(SalaryVoucherItemVo::getType));
                List<SalaryVoucherItemVo> salaryVoucherItemVoByEmployee = groupByType.get(1);
                List<SalaryVoucherItemVo> salaryVoucherItemVoBySalary = groupByType.get(0);
                List<User> userQuery = new ArrayList<>();//待查询用户信息列表
                Set<String> groupNameList = new HashSet<>();//待查询部门名称列表
                //循环计算总金额，判断，将员工信息放入待查询用户信息列表中，将部门名称放入待查询部门名称列表中
                netAmountCount = initEmployeeSalary(list,netAmountCount,salaryVoucherItemVoByEmployee,userQuery,groupNameList);
                //根据部门名称查询部门信息
                List<CompanyStructure> companyStructureList = companyAPI.searchStructureNames(companyId,groupNameList);
                //查不到部门信息提示手动添加
                if(CollectionUtils.isEmpty(companyStructureList)){
                    return Result.failed("部门信息未查询到，请手动添加");
                }
                //根据工号、名称、身份证号、手机号查询员工信息
                List<User> users = userAPI.searchByUserOrSave(companyId,userQuery);
                if(CollectionUtils.isEmpty(users)){
                    return Result.failed("员工信息自动添加失败或未查询到，请手动处理");
                }
                Salary salary = new Salary().setCompanyId(companyId).setImportDate(importDate).setTypeId(typeId).setNetAmount(netAmountCount).setStaffCount(list.size());
                salaryService.save(salary);

                initEmployeeSalary(salary.getId(),list,employeeSalaryArrayList,salaryVoucherItemVoBySalary,employeeItemExtends,users);
                log.info("{}",list);
                employeeSalaryService.saveBatch(employeeSalaryArrayList);
                employeeItemExtendService.saveBatch(employeeItemExtends);
            }
        }   catch (Exception e) {
            log.error("导入失败",e);
            return Result.failed(e.getMessage());
        }
        return success();
    }



    private Long initEmployeeSalary(List<Map<String, Object>> list, Long netAmountCount, List<SalaryVoucherItemVo> salaryVoucherItemVoByEmployee,List<User> userQuery,Set<String> groupNameList) {
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> employeeSalaryMap = list.get(i);
            //如果员工的必填数据为空，则报错
            for (SalaryVoucherItemVo salaryVoucherItemVo : salaryVoucherItemVoByEmployee) {
                if (Objects.isNull(employeeSalaryMap.get(salaryVoucherItemVo.getTypeName()))){
                    throw new RuntimeException("没有员工唯一标识数据");
                }
            }
            String jobId = Objects.nonNull(employeeSalaryMap.get(SalayExeclHeaderEnum.ID.getValue())) ? employeeSalaryMap.get(SalayExeclHeaderEnum.ID.getValue()).toString() : null;//工号
            String name = Objects.nonNull(employeeSalaryMap.get(SalayExeclHeaderEnum.NAME.getValue())) ? employeeSalaryMap.get(SalayExeclHeaderEnum.NAME.getValue()).toString() : null;//名称
            String idCard = Objects.nonNull(employeeSalaryMap.get(SalayExeclHeaderEnum.ID_NUMBER.getValue()))?employeeSalaryMap.get(SalayExeclHeaderEnum.ID_NUMBER.getValue()).toString():null;//身份证号
            Long phone = Objects.nonNull(employeeSalaryMap.get(SalayExeclHeaderEnum.PHONE.getValue()))&&StrUtil.isNotBlank(employeeSalaryMap.get(SalayExeclHeaderEnum.PHONE.getValue()).toString())? Long.valueOf(employeeSalaryMap.get(SalayExeclHeaderEnum.PHONE.getValue()).toString()) :null;//手机号
            String groupName = Objects.nonNull(employeeSalaryMap.get(SalayExeclHeaderEnum.DEPARTMENT.getValue())) ? employeeSalaryMap.get(SalayExeclHeaderEnum.DEPARTMENT.getValue()).toString() : null;//部门

            userQuery.add(new User(jobId,name,phone,idCard,groupName));
            groupNameList.add(groupName);

            Double netAmount = StringUtils.isNotEmpty(employeeSalaryMap.get(SalayExeclHeaderEnum.NET_SALARY.getValue()).toString())?Double.parseDouble(employeeSalaryMap.get(SalayExeclHeaderEnum.NET_SALARY.getValue()).toString())*100:0;//实发
            netAmountCount=netAmountCount+netAmount.longValue();
        }
        return netAmountCount;
    }

    /**
     * 初始化员工工资
     *
     * @param list
     * @param employeeSalaryArrayList
     * @param employeeItemExtends
     */
    private void initEmployeeSalary(Long salaryId,
                                    List<Map<String, Object>> list,
                                    List<EmployeeSalary> employeeSalaryArrayList,
                                    List<SalaryVoucherItemVo> salaryVoucherItemVoBySalary,
                                    List<EmployeeItemExtend> employeeItemExtends,
                                    List<User> users) {


        Map<String, Long> userMap = users.stream().collect(Collectors.toMap(o1 -> o1.getName() + o1.getPhone() + o1.getIdCard() + o1.getJobCard(), User::getId));

        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> employeeSalaryMap = list.get(i);

            Double grossAmount = StringUtils.isNotEmpty(employeeSalaryMap.get(SalayExeclHeaderEnum.TOTAL_INCOME.getValue()).toString()) ?Double.parseDouble(employeeSalaryMap.get(SalayExeclHeaderEnum.TOTAL_INCOME.getValue()).toString())*100:0;//应发
            Double netAmount = StringUtils.isNotEmpty(employeeSalaryMap.get(SalayExeclHeaderEnum.NET_SALARY.getValue()).toString())?Double.parseDouble(employeeSalaryMap.get(SalayExeclHeaderEnum.NET_SALARY.getValue()).toString())*100:0;//实发


            EmployeeSalary employeeSalary = new EmployeeSalary();
            employeeSalary.setSalaryId(salaryId);

            String jobId = Objects.nonNull(employeeSalaryMap.get(SalayExeclHeaderEnum.ID.getValue())) ? employeeSalaryMap.get(SalayExeclHeaderEnum.ID.getValue()).toString() : null;//工号
            String name = Objects.nonNull(employeeSalaryMap.get(SalayExeclHeaderEnum.NAME.getValue())) ? employeeSalaryMap.get(SalayExeclHeaderEnum.NAME.getValue()).toString() : null;//名称
            String idCard = Objects.nonNull(employeeSalaryMap.get(SalayExeclHeaderEnum.ID_NUMBER.getValue()))?employeeSalaryMap.get(SalayExeclHeaderEnum.ID_NUMBER.getValue()).toString():null;//身份证号
            String phone = Objects.nonNull(employeeSalaryMap.get(SalayExeclHeaderEnum.PHONE.getValue()))?employeeSalaryMap.get(SalayExeclHeaderEnum.PHONE.getValue()).toString():null;//手机号

            employeeSalary.setEmployeeId(userMap.get(name + phone + idCard + jobId));
            employeeSalary.setEmployeeName(name);
            employeeSalary.setIdCard(idCard);
            employeeSalary.setJobCard(jobId);
            employeeSalary.setPhone(phone);

            String groupName = Objects.nonNull(employeeSalaryMap.get(SalayExeclHeaderEnum.DEPARTMENT.getValue())) ? employeeSalaryMap.get(SalayExeclHeaderEnum.NAME.getValue()).toString() : null;//部门
            employeeSalary.setCompanyStructureName(groupName);

            employeeSalary.setGrossAmount(grossAmount.longValue());
            employeeSalary.setNetAmount(netAmount.longValue());
            employeeSalaryArrayList.add(employeeSalary);

            for (SalaryVoucherItemVo salaryVoucherItemVo:salaryVoucherItemVoBySalary){
                Double fieldContent =  employeeSalaryMap.get(salaryVoucherItemVo.getTypeName())!=null
                        && StrUtil.isNotEmpty(employeeSalaryMap.get(salaryVoucherItemVo.getTypeName()).toString())
                        ?Double.parseDouble(employeeSalaryMap.get(salaryVoucherItemVo.getTypeName()).toString())*100:0;
                employeeItemExtends.add(new EmployeeItemExtend().setSalaryId(salaryId).setEmployeeId(userMap.get(name + phone + idCard + jobId)).setContent(fieldContent.longValue()).setItemTypeId(salaryVoucherItemVo.getTypeId()));
            }
        }
    }



    @GetMapping("/salary/temp/export")
    public void export(HttpServletResponse response, @RequestParam("companyId") Long companyId) {

        List<Map<String, Object>> rows = getRows(companyId);//new ArrayList<>();

        OutputStream out = null;
        ExcelWriter writer = ExcelUtil.getWriter(new String("工资模板.xlsx".getBytes(StandardCharsets.UTF_8)));
        try {
            out = response.getOutputStream();
            writer.merge(rows.size() - 1, "资金表");
            writer.setColumnWidth(-1, 20);
            writer.write(rows, true);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
            response.setHeader("content-disposition", "attachment;fileName=" + URLEncoder.encode("资金模板.xlsx", "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            writer.flush(out, true);
            writer.close();
            IoUtil.close(out);
        }



    }

    private List<Map<String, Object>> getRows(Long companyId) {
        List<Map<String, Object>> rows = new ArrayList<>();

        new HashMap<String, Object>(){{
            put("工号", "");
            put("姓名", "");
            put("部门", "");
            put("身份证号码", "");
            put("计薪日", "");
            put("出勤天数", "");
            put("基本工资", "");
            put("出勤工资", "");
            put("奖金", "");
            put("津贴", "");
            put("补贴", "");
            put("其他应发1", "");
            put("其他应发2", "");
            put("应发工资", "");
            put("养老保险", "");
            put("医疗保险", "");
            put("失业保险", "");
            put("公积金", "");
            put("累计应发", "");
            put("累计社保公积金", "");
            put("累计子女教育", "");
            put("累计住房贷款利息", "");
            put("累计住房租金", "");
            put("累计赡养父母", "");
            put("累计继续教育", "");
            put("累计婴幼儿照护费用", "");
            put("累计应缴个税", "");
            put("累计已缴个税", "");
            put("本月应缴个税", "");
            put("个人还款", "");
            put("其他扣款1", "");
            put("其他扣款2", "");
            put("实发工资", "");
        }};
//        rows.add(new HashMap<String, Object>(){{
//
//        }});
//        rows.add(new HashMap<String, Object>(){{
//
//        }});
//        rows.add(new HashMap<String, Object>(){{
//
//        }});
//        rows.add(new HashMap<String, Object>(){{
//
//        }});
//        rows.add(new HashMap<String, Object>(){{
//
//        }});
//        rows.add(new HashMap<String, Object>(){{
//
//        }});
//        rows.add(new HashMap<String, Object>(){{
//
//        }});
//        rows.add(new HashMap<String, Object>(){{
//
//        }});
//        rows.add(new HashMap<String, Object>(){{
//
//        }});
//        rows.add(new HashMap<String, Object>(){{
//
//        }});
//        rows.add(new HashMap<String, Object>(){{
//            put("其他应发1", "");
//        }});
//        rows.add(new HashMap<String, Object>(){{
//            put("其他应发2", "");
//        }});
//        rows.add(new HashMap<String, Object>(){{
//
//        }});
//        rows.add(new HashMap<String, Object>(){{
//
//        }});
//        rows.add(new HashMap<String, Object>(){{
//
//        }});
//        rows.add(new HashMap<String, Object>(){{
//
//        }});
//        rows.add(new HashMap<String, Object>(){{
//
//        }});
//        rows.add(new HashMap<String, Object>(){{
//
//        }});
//        rows.add(new HashMap<String, Object>(){{
//
//        }});
//        rows.add(new HashMap<String, Object>(){{
//
//        }});
//        rows.add(new HashMap<String, Object>(){{
//
//        }});
//        rows.add(new HashMap<String, Object>(){{
//
//        }});
//        rows.add(new HashMap<String, Object>(){{
//
//        }});
//        rows.add(new HashMap<String, Object>(){{
//
//        }});
//        rows.add(new HashMap<String, Object>(){{
//
//        }});
//        rows.add(new HashMap<String, Object>(){{
//
//        }});
//        rows.add(new HashMap<String, Object>(){{
//
//        }});
//        rows.add(new HashMap<String, Object>(){{
//
//        }});
//        rows.add(new HashMap<String, Object>(){{
//
//        }});
//        rows.add(new HashMap<String, Object>(){{
//
//        }});
//        rows.add(new HashMap<String, Object>(){{
//
//        }});
//        rows.add(new HashMap<String, Object>(){{
//
//        }});
//        SalaryListParam salaryListParam = new SalaryListParam();
//        salaryListParam.setSize(999);
//        salaryListParam.setCurrent(1);
//        List<SalaryVoucherItemVo> salaryVoucherItemVoPage = salaryVoucherItemService.selectjoinPage(salaryListParam.toPage(), companyId, 0).getRecords();
//        salaryVoucherItemVoPage.forEach(salaryVoucherItemVo -> rows.add(new HashMap<String, Object>(){{
//            put(salaryVoucherItemVo.getTypeName(), "");
//        }}));
//        log.debug("salaryVoucherItemVoPage:{}",salaryVoucherItemVoPage);
        return rows;
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
    public Result<Boolean> remove(@PathVariable List<Long> ids){
        salaryService.getBaseMapper().deleteBatchIds(ids);
        employeeSalaryService.remove(new QueryWrapper<EmployeeSalary>().in("salary_id",ids));
        employeeItemExtendService.remove(new QueryWrapper<EmployeeItemExtend>().in("salary_id",ids));
        return success();
    }
}