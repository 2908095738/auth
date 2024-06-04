package com.bbs.financial.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.financial.entity.AssetDepreciationCertificate;
import com.bbs.financial.service.AssetDepreciationCertificateService;
import com.bbs.financial.mapper.AssetDepreciationCertificateMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 *
 */
@Service
public class AssetDepreciationCertificateServiceImpl extends ServiceImpl<AssetDepreciationCertificateMapper, AssetDepreciationCertificate>
    implements AssetDepreciationCertificateService{

    @Override
    public List<AssetDepreciationCertificate> selectList(Long id) {


        return Collections.emptyList();
    }
}




