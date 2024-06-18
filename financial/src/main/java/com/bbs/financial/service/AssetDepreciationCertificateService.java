package com.bbs.financial.service;

import com.bbs.financial.entity.AssetDepreciationCertificate;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 *
 */
public interface AssetDepreciationCertificateService extends IService<AssetDepreciationCertificate> {

    List<AssetDepreciationCertificate> selectList(Long id);
}
