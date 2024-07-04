package com.bbs.financial.service;

import com.bbs.financial.entity.Certificate;
import com.github.yulichang.base.MPJBaseService;

import java.util.Date;
import java.util.List;


/**
 * 记账凭证Service接口
 * @author vctgo
 * @date 2024-05-13
 */
public interface CertificateService extends MPJBaseService<Certificate> {

    List<Certificate> searchByCreate(Date startDate, Date endDate, String no);
}