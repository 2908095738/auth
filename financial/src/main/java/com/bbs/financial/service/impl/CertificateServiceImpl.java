package com.bbs.financial.service.impl;

import com.bbs.financial.entity.Account;
import com.bbs.financial.entity.Certificate;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.mapper.CertificateMapper;
import com.bbs.financial.service.CertificateService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 记账凭证Service业务层处理
 *
 * @author vctgo
 * @date 2024-05-13
 */
@Service
public class CertificateServiceImpl extends MPJBaseServiceImpl<CertificateMapper, Certificate> implements CertificateService
{
    @Override
    public List<Certificate> searchByCreate(Date startDate, Date endDate, String no) {
        return selectJoinList(Certificate.class, new MPJLambdaWrapper<Certificate>()
                .selectAll(Certificate.class)
                .leftJoin(CertificateAbstract.class, CertificateAbstract::getCertificateId, Certificate::getId, ext -> ext
                        .selectAssociation(CertificateAbstract.class, Certificate::getAbstracts)
                )
                .leftJoin(Account.class, Account::getId, CertificateAbstract::getAccountId)
                .ge(Certificate::getCreateTime, startDate)
                .lt(Certificate::getCreateTime, endDate)
                .eq(Account::getNo, no)
        );
    }
}
