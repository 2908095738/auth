package com.bbs.financial.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.financial.dto.IOTotalDto;
import com.bbs.financial.entity.*;
import com.bbs.financial.service.CashierService;
import com.bbs.financial.service.CertificateService;
import com.bbs.financial.service.NoteService;
import com.bbs.financial.service.ZhangHuService;
import com.bbs.financial.util.LoginUser;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Service
public class CashierServiceImpl implements CashierService {
    @Resource
    private ZhangHuService zhService;

    @Resource
    private CertificateService certificateService;

    @Resource
    private NoteService noteService;

    @Override
    public Page<Certificate> listCertificate(Integer current, Integer size, Long accountingSetId, Collection<Long> zhIdList, Long pastDateLong, Long startDateLong, Long endDateLong, boolean isMonth, boolean isPage) {
        //TODO L SQL合一

        //SQL待使用凭证id列表
        boolean isZhIdList = Objects.nonNull(zhIdList) && !zhIdList.isEmpty();//账户id列表是否有值
        List<Long> inCertId = getSimpleNote(zhIdList, isZhIdList).stream().map(Note::getCertificateId).collect(Collectors.toList());

        MPJLambdaWrapper<Certificate> wrappers = getDataWrapperByCertList();

        if (isMonth && !isZhIdList && isPage) {
            return certificateService.selectJoinListPage(new Page<>(current, size), Certificate.class, getConditionByCertList(wrappers, accountingSetId, inCertId, isMonth, pastDateLong, startDateLong, endDateLong));
        } else {
            return new Page<Certificate>().setRecords(certificateService.selectJoinList(Certificate.class,
                    getConditionByCertList(wrappers, accountingSetId, inCertId, isMonth, pastDateLong, startDateLong, endDateLong)));
        }
        }

    /**
     * 获取仅有凭证id、账户id的日记账列表
     *
     * @param zhIdList 账户id列表
     */
    private List<Note> getSimpleNote(Collection<Long> zhIdList, boolean isZhIdList) {
        return noteService.selectJoinList(Note.class,
                new MPJLambdaWrapper<Note>()
                        .select(Note::getCertificateId)
                        .select(Note::getZhId)
                        .eq(Note::getAccountingSetId, LoginUser.getLoginSetId())
                        .in(isZhIdList, Note::getZhId, zhIdList));
    }

    @Override
    public Page<IOTotalDto> getZhDataByTotal(Integer current, Integer size, Long zhId) {
        boolean isZhId = !ObjectUtils.isEmpty(zhId) && (zhId > NumberUtils.LONG_ZERO);
        Page<ZhangHu> tmpPage = zhService.selectJoinListPage(new Page<>(current, size), ZhangHu.class,
                new MPJLambdaWrapper<ZhangHu>()
                        .selectAll(ZhangHu.class)

                        .selectAssociation(PriceType.class, ZhangHu::getPriceType)
                        .leftJoin(PriceType.class, PriceType::getId, ZhangHu::getMTypeId)

                        .eq(ZhangHu::getIsActive, Boolean.TRUE)
                        .eq(ZhangHu::getAccountingSetId, LoginUser.getLoginSetId())
                        .eq(isZhId, ZhangHu::getId, zhId)
        );

        List<IOTotalDto> resultList = new ArrayList<>();
        tmpPage.getRecords().forEach(z -> {
            IOTotalDto dto = new IOTotalDto();
            dto.setZhId(z.getId());
            dto.setZhCode(z.getCode());
            dto.setZhName(z.getName());
            dto.setMTypeName(z.getPriceType().getName());

            resultList.add(dto);
        });

        return new Page<IOTotalDto>()
                .setCurrent(current)
                .setSize(size)
                .setTotal(tmpPage.getTotal())
                .setRecords(resultList);
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
     * @param accountingSetId 账套id
     * @param inCertId  SQL待使用凭证id列表
     * @param isMonth   true：当月；false：当月及之前
     * @param pastDateLong  当前日期之前时间戳
     * @param startDateLong 起始时间时间戳
     * @param endDateLong   结束时间时间戳
     */
    private MPJLambdaWrapper<Certificate> getConditionByCertList(MPJLambdaWrapper<Certificate> wrappers, Long accountingSetId, List<Long> inCertId, boolean isMonth, Long pastDateLong, Long startDateLong, Long endDateLong) {
        return wrappers
                .eq(Certificate::getAccountingSetId, accountingSetId)
                .in(inCertId.size() > 0, Certificate::getId, inCertId)

                .and(nonNull(startDateLong), ext -> ext
                        .ge(Certificate::getDate, new Date(startDateLong))
                        .lt(nonNull(endDateLong), Certificate::getDate, new Date(endDateLong))
                )

                .lt(!isMonth, Certificate::getDate, DateUtil.beginOfMonth(nonNull(pastDateLong) ? new Date(pastDateLong) : new Date()))

                .orderByAsc(Certificate::getCreateTime);
    }
}