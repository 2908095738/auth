package com.bbs.financial.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.api.auth.User;
import com.bbs.api.auth.UserAPI;
import com.bbs.api.auth.company.CompanyAPI;
import com.bbs.financial.converter.EmployeeSalaryConverter;
import com.bbs.financial.dto.SalaryTemplate;
import com.bbs.financial.entity.EmployeeItemExtend;
import com.bbs.financial.entity.EmployeeSalary;
import com.bbs.financial.entity.Salary;
import com.bbs.financial.enums.SalaryExcelHeaderEnum;
import com.bbs.financial.service.EmployeeItemExtendService;
import com.bbs.financial.service.EmployeeSalaryService;
import com.bbs.financial.service.SalaryService;
import com.bbs.financial.service.SalaryVoucherItemService;
import com.bbs.financial.util.LoginUser;
import com.bbs.financial.vo.SalaryVo;
import com.bbs.financial.vo.SalaryVoucherItemVo;
import com.bbs.vo.BaseParam;
import com.bbs.vo.CompanyStructure;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
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

    @Resource
    private EmployeeSalaryConverter employeeSalaryConverter;

    @Resource
    private TransactionDefinition transactionDefinition;
    @Resource
    private DataSourceTransactionManager transactionManager;



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

        private Long LoginSetId;


    }

    /**
     * 查询工资列表
     */
    @GetMapping("/salary/list")
    public Result<Page<SalaryVo>> list(SalaryListParam param)
    {
        Long loginSetId = LoginUser.getLoginSetId();
        param.setLoginSetId(loginSetId);
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
    public Result<Boolean> add(@RequestParam("importDate") String importDate,@RequestParam("typeId") Long typeId,@RequestParam("file") MultipartFile file){
        Long loginSetId = LoginUser.getLoginSetId();
        Long netAmountCount = 0L;
        List<EmployeeSalary> employeeSalaryArrayList = new ArrayList<>();
        List<EmployeeItemExtend> employeeItemExtends = new ArrayList<>();
        Long accountingSetId = loginSetId;
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);

        try {
            ExcelReader reader = ExcelUtil.getReader(file.getInputStream(),0);
            reader.setIgnoreEmptyRow(true);
            List<SalaryTemplate> list = reader.read(2,3,SalaryTemplate.class );
            //过滤掉姓名为合计的数据
            list = list.stream().filter(item->!StrUtil.equals(item.getName(),"合计")).collect(Collectors.toList());
            log.debug("list:{}",list);
            if(CollUtil.isNotEmpty(list)){
                Map<Integer, List<SalaryVoucherItemVo>> groupByType = salaryVoucherItemService.selectjoinByIsActive(accountingSetId, 1).stream().collect(Collectors.groupingBy(SalaryVoucherItemVo::getType));
                List<SalaryVoucherItemVo> salaryVoucherItemVoBySalary = groupByType.get(0);
                List<User> userQuery = new ArrayList<>();//待查询用户信息列表
                Set<String> groupNameList = new HashSet<>();//待查询部门名称列表
                //循环计算总金额，判断，将员工信息放入待查询用户信息列表中，将部门名称放入待查询部门名称列表中
                netAmountCount = initEmployeeSalary(list,netAmountCount,userQuery,groupNameList);
                //根据部门名称查询部门信息
                List<CompanyStructure> companyStructureList = companyAPI.searchStructureNames(accountingSetId,groupNameList);
                //查不到部门信息提示手动添加
                if(CollUtil.isEmpty(companyStructureList)){
                    return Result.failed("部门信息未查询到，请手动添加");
                }
                //根据工号、名称、身份证号、手机号查询员工信息
                List<User> users = userAPI.searchByUserOrSave(accountingSetId,userQuery);
                if(CollUtil.isEmpty(users)){
                    return Result.failed("员工信息自动添加失败或未查询到，请手动处理");
                }
                Salary salary = new Salary().setAccountingSetId(accountingSetId).setImportDate(importDate).setTypeId(typeId).setNetAmount(netAmountCount).setStaffCount(list.size());
                salaryService.save(salary);

                initEmployeeSalary(salary.getId(),list,employeeSalaryArrayList,salaryVoucherItemVoBySalary,employeeItemExtends,users);
                log.info("{}",list);
                employeeSalaryService.saveBatch(employeeSalaryArrayList);
                employeeItemExtendService.saveBatch(employeeItemExtends);
                transactionManager.commit(transaction);
            }
        }   catch (Exception e) {
            log.error("导入失败",e);
            transactionManager.rollback(transaction);
            return Result.failed(e.getMessage());
        }
        return success();
    }



    private Long initEmployeeSalary(List<SalaryTemplate> list, Long netAmountCount,List<User> userQuery,Set<String> groupNameList) {
        for (int i = 0; i < list.size(); i++) {
            SalaryTemplate employeeSalaryTemplate = list.get(i);
            String jobId = StrUtil.isNotBlank(employeeSalaryTemplate.getJobId()) ? employeeSalaryTemplate.getJobId() : null;//工号
            String name = StrUtil.isNotBlank(employeeSalaryTemplate.getName()) ? employeeSalaryTemplate.getName() : null;//名称
            String idCard = StrUtil.isNotBlank(employeeSalaryTemplate.getIdCard())?employeeSalaryTemplate.getIdCard():null;//身份证号
            Long phone = StrUtil.isNotBlank(employeeSalaryTemplate.getPhone())?Long.valueOf(employeeSalaryTemplate.getPhone()):null;//手机号
            String groupName = StrUtil.isNotBlank(employeeSalaryTemplate.getGroupName()) ? employeeSalaryTemplate.getGroupName() : null;//部门

            userQuery.add(new User(jobId,name,phone,idCard,groupName));
            groupNameList.add(groupName);

            BigDecimal netAmount = ObjUtil.isNotEmpty(employeeSalaryTemplate.getNetPay())?employeeSalaryTemplate.getNetPay().multiply(BigDecimal.valueOf(100)):BigDecimal.ZERO;//实发
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
                                    List<SalaryTemplate> list,
                                    List<EmployeeSalary> employeeSalaryArrayList,
                                    List<SalaryVoucherItemVo> salaryVoucherItemVoBySalary,
                                    List<EmployeeItemExtend> employeeItemExtends,
                                    List<User> users) throws IllegalAccessException {

        Map<String, Long> userMap = users.stream().collect(Collectors.toMap(o1 -> o1.getName() + o1.getPhone() + o1.getIdCard() + o1.getJobCard(), User::getId));

        for (int i = 0; i < list.size(); i++) {
            SalaryTemplate employeeSalaryTemplate = list.get(i);

//            BigDecimal grossAmount = ObjUtil.isNotEmpty(employeeSalaryTemplate.grossPay) ? employeeSalaryTemplate.grossPay.multiply(BigDecimal.valueOf(100)):BigDecimal.ZERO;//应发
//            BigDecimal netAmount = ObjUtil.isNotEmpty(employeeSalaryTemplate.netPay)?employeeSalaryTemplate.netPay.multiply(BigDecimal.valueOf(100)):BigDecimal.ZERO;//实发

            EmployeeSalary employeeSalary = employeeSalaryConverter.toEntity(employeeSalaryTemplate);
            employeeSalary.setSalaryId(salaryId);

            String jobId = StrUtil.isNotBlank(employeeSalaryTemplate.getJobId()) ? employeeSalaryTemplate.getJobId() : null;//工号
            String name = StrUtil.isNotBlank(employeeSalaryTemplate.getName()) ? employeeSalaryTemplate.getName() : null;//名称
            String idCard = StrUtil.isNotBlank(employeeSalaryTemplate.getIdCard())?employeeSalaryTemplate.getIdCard():null;//身份证号
            Long phone = StrUtil.isNotBlank(employeeSalaryTemplate.getPhone())?Long.valueOf(employeeSalaryTemplate.getPhone()):null;//手机号
            Long employeeId = userMap.get(name + phone + idCard + jobId);
            employeeSalary.setEmployeeId(employeeId);
            employeeSalary.setEmployeeName(name);
            employeeSalary.setJobCard(jobId);

            String groupName = StrUtil.isNotBlank(employeeSalaryTemplate.getGroupName()) ? employeeSalaryTemplate.getGroupName() : null;//部门
            employeeSalary.setCompanyStructureName(groupName);

            employeeSalaryArrayList.add(employeeSalary);

            Field[] fields = ReflectUtil.getFields(SalaryTemplate.class);// 获取所有字段
            //把fields按属性名转成map
            Map<String, Field> fieldMap = Arrays.stream(fields).collect(Collectors.toMap(Field::getName, Function.identity()));

            for (SalaryVoucherItemVo salaryVoucherItemVo:salaryVoucherItemVoBySalary){
                String fieldName = SalaryExcelHeaderEnum.getFieldNameByDisplayName(salaryVoucherItemVo.getTypeName());//属性名
                Field field = fieldMap.get(fieldName);
                if (field==null){
                    continue;
                }
                field.setAccessible(true);
                Object beforValue = field.get(employeeSalaryTemplate);
                Double fieldContent = beforValue!=null&&StrUtil.isNotEmpty(beforValue.toString())?Double.parseDouble(beforValue.toString())*100:0;
                employeeItemExtends.add(new EmployeeItemExtend().setSalaryId(salaryId).setEmployeeId(employeeId).setContent(fieldContent.longValue()).setItemTypeId(salaryVoucherItemVo.getTypeId()));
            }
        }
    }



    @GetMapping("/salary/temp/export")
    public void export(HttpServletResponse response) {
        OutputStream out = null;
        String filePath = "template/salary.xlsx";

        try {
//            String realPath = getServletContext().getRealPath("/WEB-INF/template/salary.xlsx");
//            File file = new File(realPath);
            InputStream in = getClass().getClassLoader().getResourceAsStream(filePath);
//            InputStream in = FileUtil.getInputStream(classPathResource.getFile());
            out = response.getOutputStream();
            // 文件名应该进行URL编码，以防文件名中存在特殊字符
            String fileName = "downloaded_salary.xlsx";
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

            // 将文件内容写入响应流
            IoUtil.copy(in, out);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            IoUtil.close(out);
        }



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