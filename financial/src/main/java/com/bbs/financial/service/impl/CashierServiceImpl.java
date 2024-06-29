package com.bbs.financial.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.financial.entity.*;
import com.bbs.financial.service.CashierService;
import com.bbs.financial.service.CertificateService;
import com.bbs.financial.service.ZhangHuService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static java.util.Objects.nonNull;

@Service
public class CashierServiceImpl implements CashierService {
    @Resource
    private ZhangHuService zhService;

    @Resource
    private CertificateService certificateService;

    @Override
    public Page<Certificate> listCertificate(Integer current, Integer size, Long companyId, Long zhangHuId, String dateStr, Long startDateLong, Long endDateLong, boolean isMonth, boolean isPage) {
        //TODO L SQL合一

        //SQL待使用凭证id列表
        List<Long> inCertId = Collections.emptyList();
        if (!ObjectUtils.isEmpty(zhangHuId) && (zhangHuId > 0)) {
            //查询该账户对应凭证
            inCertId = zhService.selectJoinList(Long.class,
                    new MPJLambdaWrapper<ZhangHu>()
                            .select(CertificateAbstract::getCertificateId)
                            .eq(!ObjectUtils.isEmpty(zhangHuId) && zhangHuId > 0, ZhangHu::getId, zhangHuId)
                            .leftJoin(CertificateAbstract.class, CertificateAbstract::getAccountId, ZhangHu::getSubjectsId));
        }

        MPJLambdaWrapper<Certificate> wrappers = getDataWrapperByCertList();

        boolean nonZhId = !ObjectUtils.isEmpty(zhangHuId) && (zhangHuId == 0);
        if (isMonth && nonZhId && isPage) {
            return certificateService.selectJoinListPage(new Page<>(current, size), Certificate.class, getConditionByCertList(wrappers, companyId, inCertId, isMonth, dateStr, startDateLong, endDateLong));
        } else {
            return new Page<Certificate>().setRecords(certificateService.selectJoinList(Certificate.class,
                    getConditionByCertList(wrappers, companyId, inCertId, isMonth, dateStr, startDateLong, endDateLong)));
        }
    }

    /**
     * 获取凭证列表输出实例域的Wrapper
     */
    private MPJLambdaWrapper<Certificate> getDataWrapperByCertList() {
        return new MPJLambdaWrapper<Certificate>()
                .selectAll(Certificate.class)
                .selectCollection(CertificateAbstract.class, Certificate::getAbstracts, ext -> ext
                        .association(Account.class, CertificateAbstract::getAccount)
                )
                .selectCollection(CertificateFile.class, Certificate::getFiles)

                // left join 凭证科目表
                .leftJoin(CertificateAbstract.class, CertificateAbstract::getCertificateId, Certificate::getId)
                // left join 科目表
                .leftJoin(Account.class, Account::getId, CertificateAbstract::getAccountId)
                // left join 附件表
                .leftJoin(CertificateFile.class, CertificateFile::getCertificateId, Certificate::getId);
    }

    /**
     * 获取查询凭证列表条件
     *
     * @param companyId 公司id
     * @param inCertId  SQL待使用凭证id列表
     * @param isMonth   true：当月；false：当月及之前
     * @param dateStr       时间戳字符串
     * @param startDateLong 起始时间时间戳
     * @param endDateLong   结束时间时间戳
     */
    private MPJLambdaWrapper<Certificate> getConditionByCertList(MPJLambdaWrapper<Certificate> wrappers, Long companyId, List<Long> inCertId, boolean isMonth, String dateStr, Long startDateLong, Long endDateLong) {
        return wrappers
                .eq(Certificate::getCompanyId, companyId)
                .in(inCertId.size() > 0, Certificate::getId, inCertId)

                .and(nonNull(startDateLong), ext -> ext
                        .ge(Certificate::getDate, new Date(startDateLong))
                        .lt(nonNull(endDateLong), Certificate::getDate, new Date(endDateLong))
                )

                .lt(!isMonth, Certificate::getDate, DateUtil.beginOfMonth(nonNull(dateStr) ? new Date(Long.parseLong(dateStr)) : new Date()))

                .orderByAsc(Certificate::getCreateTime);
    }
}