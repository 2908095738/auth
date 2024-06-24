package com.bbs.financial.service;

import com.bbs.financial.entity.Certificate;
import com.github.yulichang.base.MPJBaseService;
import com.github.yulichang.extension.mapping.base.MPJDeepService;

import java.util.List;


/**
 * 记账凭证Service接口
 * @author vctgo
 * @date 2024-05-13
 */
public interface CertificateService extends MPJBaseService<Certificate> {

    List<Certificate> selectByDepreciation();
}