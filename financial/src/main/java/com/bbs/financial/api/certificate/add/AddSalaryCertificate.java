package com.bbs.financial.api.certificate.add;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjUtil;
import com.bbs.Result;
import com.bbs.enums.financial.CertificateWordEnum;
import com.bbs.financial.converter.CertificateConverter;
import com.bbs.financial.entity.Certificate;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.entity.CertificateTemplate;
import com.bbs.financial.entity.Salary;
import com.bbs.financial.enums.BorrowOrLoansType;
import com.bbs.financial.enums.CertificateType;
import com.bbs.financial.service.CertificateAbstractService;
import com.bbs.financial.service.CertificateService;
import com.bbs.financial.service.CertificateTemplateService;
import com.bbs.financial.service.SalaryService;
import com.bbs.financial.util.LoginUser;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;

@RestController
public class AddSalaryCertificate {

    @Resource
    private CertificateService db;
    @Resource
    private CertificateAbstractService certificateAbstractService;
    @Resource
    private TransactionDefinition transactionDefinition;
    @Resource
    private DataSourceTransactionManager transactionManager;
    @Resource
    private CertificateConverter converter;
    @Resource
    private SalaryService salaryService;
    @Resource
    private CertificateTemplateService certificateTemplateService;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {

        /**
         * 工资ID
         */
        private Long salaryId;

        /**
         * 模板名称
         */
        private List<String> templateNames;
        /**
         * 模板规则
         */
        private String rule;
    }


    @PutMapping("/salary/certificate")
    public Result<Boolean> add(@RequestBody Param param) {
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        List<CertificateTemplate> template = certificateTemplateService.getJoinTemplate(LoginUser.getCompanyId(), param.templateNames);
        if (CollUtil.isEmpty(template)) {
            throw new RuntimeException("模板不存在");
        }
        long no = db.lambdaQuery().eq(Certificate::getCompanyId, LoginUser.getCompanyId())
                .ge(Certificate::getCreateTime, DateUtil.beginOfMonth(new Date()))
                .lt(Certificate::getCreateTime, DateUtil.beginOfMonth(DateUtil.offsetMonth(new Date(), INTEGER_ONE)))
                .count() + INTEGER_ONE;
        Map<String, Long> certificateTypeAndId = new HashMap<>();
        try {
            for (CertificateTemplate certificateTemplate : template) {
                Certificate certificate = new Certificate();
                certificate.setCompanyId(LoginUser.getCompanyId());
                certificate.setCertificateWord(CertificateWordEnum.RECORD);
                certificate.setNo(no);
                certificate.setDate(new Date());
                certificate.setCreateBy(LoginUser.getId());
                db.save(certificate);
                certificateAbstractService.saveBatch(certificateTemplate.getTemplateAbstractList().stream().map(certificateAbstract -> {
                    Long useField = certificateAbstract.getUseField();//取值
                    String useEmployee = certificateAbstract.getUseEmployee();//应用人员范围
                    Integer salaryType = certificateAbstract.getSalaryType();//类型
                    if(ObjUtil.isNull(useField)){
                        throw new RuntimeException(certificateTemplate.getType()+"模板没有设置取值字段！");
                    }
                    CertificateAbstract entity = new CertificateAbstract();
                    entity.setCertificateId(certificate.getId());
                    entity.setCertificateAbstract(certificateAbstract.getCertificateAbstract());
                    entity.setAccountId(certificateAbstract.getAccountId());

                    Long money = salaryService.countMoneyByTemplate(param.salaryId,useField,salaryType,useEmployee).stream().mapToLong(Long::longValue).sum();
                    if(Objects.equals(certificateAbstract.getBorrowOrLoansType(), BorrowOrLoansType.BORROW.getKey())){
                        entity.setBorrowMoney(money);
                    } else if (Objects.equals(certificateAbstract.getBorrowOrLoansType(), BorrowOrLoansType.LOANS.getKey())) {
                        entity.setLoansMoney(money);
                    }
                    return entity;
                }).collect(Collectors.toList()));
                no+=INTEGER_ONE;
                certificateTypeAndId.put(certificateTemplate.getType(),certificate.getId());
            }

            //删除旧凭证
            for (String templateName : param.templateNames) {
                if(templateName.equals("jc")){
                    db.deleteJoin(new MPJLambdaWrapper<Certificate>()
                            .leftJoin(Salary.class,"jc",Salary::getJCertificateId,Certificate::getId)
                            .eq(Salary::getId,param.salaryId)
                    );
                    certificateAbstractService.deleteJoin(new MPJLambdaWrapper<CertificateAbstract>()
                            .leftJoin(Certificate.class,Certificate::getId,CertificateAbstract::getCertificateId)
                            .leftJoin(Salary.class,"jc",Salary::getJCertificateId,Certificate::getId)
                            .eq(Salary::getId,param.salaryId)
                    );
                }
                if(templateName.equals("fc")){
                    db.deleteJoin(new MPJLambdaWrapper<Certificate>()
                            .leftJoin(Salary.class,"fc",Salary::getFCertificateId,Certificate::getId)
                            .eq(Salary::getId,param.salaryId)
                    );
                    certificateAbstractService.deleteJoin(new MPJLambdaWrapper<CertificateAbstract>()
                            .leftJoin(Certificate.class,Certificate::getId,CertificateAbstract::getCertificateId)
                            .leftJoin(Salary.class,"fc",Salary::getFCertificateId,Certificate::getId)
                            .eq(Salary::getId,param.salaryId)
                    );
                }

            }
            Salary salary = new Salary().setId(param.getSalaryId())
                    .setFCertificateId(certificateTypeAndId.getOrDefault(CertificateType.PAY_A_SALARY.getValue(),null))
                    .setJCertificateId(certificateTypeAndId.getOrDefault(CertificateType.ACCRUED_SALARY.getValue(),null));
            salaryService.updateById(salary);

            Set<String> collect = template.stream().map(certificateTemplate -> {
                if (!param.templateNames.contains(certificateTemplate.getType())) {
                    return certificateTemplate.getType();
                }
                return null;
            }).collect(Collectors.toSet());
            if (CollUtil.isNotEmpty(collect)&& ObjUtil.isNotEmpty(collect.toArray()[0])) {
                throw new RuntimeException(String.join(",", collect) + "模板不存在");
            }
            transactionManager.commit(transaction);
            return Result.success();
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            return Result.failed(e.getMessage());
        }
    }
}
