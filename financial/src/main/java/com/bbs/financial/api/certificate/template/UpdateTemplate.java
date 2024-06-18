package com.bbs.financial.api.certificate.template;

import cn.hutool.core.bean.BeanUtil;
import com.bbs.Result;
import com.bbs.financial.entity.CertificateTemplate;
import com.bbs.financial.entity.CertificateTemplateAbstract;
import com.bbs.financial.service.CertificateTemplateAbstractService;
import com.bbs.financial.service.CertificateTemplateService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@RequestMapping
@RestController("UpdateCertificateTemplate")
public class UpdateTemplate {

    @Resource
    private CertificateTemplateService certificateTemplateService;
    @Resource
    private TransactionDefinition transactionDefinition;
    @Resource
    private DataSourceTransactionManager transactionManager;
    @Resource
    private CertificateTemplateAbstractService certificateTemplateAbstractService;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Param {

        /**
         * 模板ID
         */
        @NotNull
        private Long id;

        /**
         * 类型
         */
        private String type;

        /**
         * 名称
         */
        private String name;

        /**
         * 简介
         */
        private String comment;

        /**
         * 具体科目摘要
         */
        private List<AddTemplate.TemplateAbstract> abstractList;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TemplateAbstract {
        /**
         * id
         */
        @NotNull
        private Long id;
        /**
         * 摘要
         */
        private String certificateAbstract;

        /**
         * 科目ID
         */
        private Long accountId;

        /**
         * 辅助核算
         */
        private String auxiliary;

        /**
         * 数量
         */
        private Long number;

        /**
         * 币别ID
         */
        private Long moneyTypeId;

        /**
         * 借方金额
         */
        private String borrowMoney;

        /**
         * 贷方金额
         */
        private String loansMoney;

        /**
         * 权重
         */
        private Integer weight;
    }

    @PostMapping("/certificate/template")
    public Result<Boolean> update(@RequestBody Param param) {
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            certificateTemplateService.lambdaUpdate()
                    .set(isNotBlank(param.name), CertificateTemplate::getName, param.name)
                    .set(isNotBlank(param.type), CertificateTemplate::getType, param.type)
                    .set(isNotBlank(param.comment), CertificateTemplate::getComment, param.comment)
                    .eq(CertificateTemplate::getId, param.id)
                    .update();
            certificateTemplateAbstractService.updateBatchById(param.abstractList.stream().map(templateAbstract -> {
                CertificateTemplateAbstract entity = new CertificateTemplateAbstract();
                BeanUtil.copyProperties(templateAbstract, entity);
                entity.setBorrowMoney(Long.valueOf(templateAbstract.getBorrowMoney().replace(",", "")));
                entity.setLoansMoney(Long.valueOf(templateAbstract.getLoansMoney().replace(",", "")));
                return entity;
            }).collect(Collectors.toList()));
            transactionManager.commit(transaction);
            return Result.success();
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new RuntimeException(e);
        }
    }
}
