package com.bbs.financial.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.bbs.financial.entity.Certificate;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.mapper.CertificateMapper;
import com.bbs.financial.service.CertificateService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;

/**
 * 记账凭证Service业务层处理
 *
 * @author vctgo
 * @date 2024-05-13
 */
@Service
public class CertificateServiceImpl extends MPJBaseServiceImpl<CertificateMapper, Certificate> implements CertificateService
{
    /**
     * 查询当前月 折旧 凭证
     * @return
     */
    @Override
    public Certificate selectByNowDepreciation() {
        List<Certificate> list =selectJoinList(Certificate.class, new MPJLambdaWrapper<Certificate>()
                .selectAll(Certificate.class)
                .selectCollection(CertificateAbstract.class, Certificate::getAbstracts)
                .leftJoin(CertificateAbstract.class, CertificateAbstract::getCertificateId, Certificate::getId)
                .eq(Certificate::getType,INTEGER_ONE)
                .between(Certificate::getCreateTime, DateUtil.format(DateUtil.beginOfMonth(DateUtil.date()), "yyyy-MM-dd"), DateUtil.date())
        );
        if(CollUtil.isNotEmpty(list)){
            return list.get(0);
        }
        return null;
    }
}
