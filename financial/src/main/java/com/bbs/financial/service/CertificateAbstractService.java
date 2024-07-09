package com.bbs.financial.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.financial.entity.CertificateAbstract;
import com.github.yulichang.base.MPJBaseService;

import java.util.Date;
import java.util.List;

/**
* @author 路晨霖
* @description 针对表【certificate_abstract(记账凭证摘要)】的数据库操作Service
* @createDate 2024-05-10 23:34:18
*/
public interface CertificateAbstractService extends MPJBaseService<CertificateAbstract> {

    Page<CertificateAbstract> selectPage(Long companyId, String certificateCreateTime, Long accountId, Integer current, Integer size);

    void initDataByNo(Page<CertificateAbstract> list);

    void initDataByMonth(List<CertificateAbstract> list);

    List<CertificateAbstract> selectList(Long companyId, Date certificateStartCreateTime, Date certificateEndCreateTime, Long accountId);
}
