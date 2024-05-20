package com.bbs.financial.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.bbs.financial.entity.Certificate;
import com.bbs.financial.entity.EmployeeSalary;
import lombok.Data;

import java.util.List;

@Data
public class SalaryVo {

    /**
     *
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 公司id
     */
    @TableField(value = "c_id")
    private Long cId;

    /**
     * 导入日期
     */
    @TableField(value = "Import_date")
    private String importDate;

    /**
     * 关联的工资类型
     */
    @TableField(value = "type_id")
    private Long typeId;

    /**
     * 员工数
     */
    @TableField(value = "staff_count")
    private Integer staffCount;

    /**
     * 总工资金额
     */
    @TableField(value = "net_amount")
    private Long netAmount;

    /**
     * 计提凭证
     */
    @TableField(exist = false)
    private Certificate jCertificate;

    /**
     * 发放凭证
     */
    @TableField(exist = false)
    private Certificate fCertificate;


    @TableField(exist = false)
    private List<EmployeeSalary> employeeSalaries;


}
