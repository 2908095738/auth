package com.bbs.financial.api.invoice.update;

import cn.hutool.core.bean.BeanUtil;
import com.bbs.Result;
import com.bbs.financial.converter.CertificateTemplateConverter;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 更新凭证模板
 */
@RestController
@RequestMapping
public class UpdateCertTemp {

    @Resource
    private DataSourceTransactionManager transactionManager;

    @Resource
    private TransactionDefinition transactionDefinition;

    @Resource
    private CertificateTemplateConverter tempConverter;

    @Resource
    private CertificateTemplateService tempORM;

    @Resource
    private CertificateTemplateAbstractService tempAbstORM;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {

        /**
         * 凭证模板唯一标识符
         */
        private Long id;

        /**
         * 模板名称
         */
        private String name;

        /**
         * 凭证字
         */
        private String certificateWord;

        /**
         * 凭证模板摘要列表
         */
        private List<DetailParam> abstractList;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailParam {
        /**
         * 摘要
         */
        private String certificateAbstract;

        /**
         * 科目id
         */
        private Long accountId;

        /**
         * 借贷类型：1借0贷，默认借
         */
        private Integer borrowOrLoansType;

        /**
         * 取值类型：0.价税合计;1.税额;2.不含税金额;
         */
        private Integer moneyType;
    }

    /**
     * 更新凭证模板
     */
    @PostMapping("/invoice/temp")
    public Result<Boolean> update(@RequestBody Param param) {
        CertificateTemplate certTemp = tempConverter.toEntity(param);

        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            tempORM.lambdaUpdate()
                    .eq(CertificateTemplate::getId, param.getId())
                    .update(certTemp);

            tempAbstORM.lambdaUpdate()
                    .eq(CertificateTemplateAbstract::getTemplateId, param.getId())
                    .remove();

            tempAbstORM.saveBatch(param.abstractList.stream().map(templateAbstract -> {
                CertificateTemplateAbstract entity = new CertificateTemplateAbstract();
                BeanUtil.copyProperties(templateAbstract, entity);
                entity.setTemplateId(param.getId());
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