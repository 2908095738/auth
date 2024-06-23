package com.bbs.financial.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.financial.entity.Account;
import com.bbs.financial.entity.Certificate;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.mapper.CertificateAbstractMapper;
import com.bbs.financial.service.CertificateAbstractService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
* @author 路晨霖
* @description 针对表【certificate_abstract(记账凭证摘要)】的数据库操作Service实现
* @createDate 2024-05-10 23:34:18
*/
@Service
public class CertificateAbstractServiceImpl extends MPJBaseServiceImpl<CertificateAbstractMapper, CertificateAbstract>
    implements CertificateAbstractService{

    @Override
    public Page<CertificateAbstract> selectPage(Long companyId, String certificateCreateTime, Long accountId, Integer current, Integer size) {
        return selectJoinListPage(new Page<>(current, size), CertificateAbstract.class, new MPJLambdaWrapper<CertificateAbstract>()
                .selectAll(CertificateAbstract.class)
                .selectAssociation(Certificate.class, CertificateAbstract::getCertificate)
                .rightJoin(Certificate.class, Certificate::getId, CertificateAbstract::getCertificateId)
                .selectAssociation(Account.class, CertificateAbstract::getAccount)
                .rightJoin(Account.class, Account::getId, CertificateAbstract::getAccountId)
                .eq(Objects.nonNull(accountId),CertificateAbstract::getAccountId,accountId)
                .eq(Certificate::getCompanyId,companyId)
                .like(Certificate::getCreateTime,certificateCreateTime)
                .orderByAsc(Certificate::getCreateTime)
        );
    }


}




