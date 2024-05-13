package com.bbs.financial.service.impl;

import com.bbs.financial.mapper.CertificateMapper;
import com.bbs.financial.entity.Certificate;
import com.bbs.financial.service.CertificateService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 记账凭证Service业务层处理
 *
 * @author vctgo
 * @date 2024-05-13
 */
@Service
public class CertificateServiceImpl extends MPJBaseServiceImpl<CertificateMapper, Certificate> implements CertificateService
{
}
