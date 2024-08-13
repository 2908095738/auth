package com.bbs.financial.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.financial.entity.*;
import com.bbs.financial.enums.InvCateEnum;
import com.bbs.financial.enums.InvStatusEnum;
import com.bbs.financial.mapper.InvoiceMapper;
import com.bbs.financial.service.CertificateTemplateService;
import com.bbs.financial.service.InvoiceService;
import com.bbs.financial.util.LoginUser;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author Mafty
 * @description 针对表【invoice(发票)】的数据库操作Service实现
 * @createDate 2024-06-20 14:36:30
 */
@Service
public class InvoiceServiceImpl extends MPJBaseServiceImpl<InvoiceMapper, Invoice>
        implements InvoiceService {

    @Resource
    private CertificateTemplateService certTempORM;

    private class StringTIP {
        public static final String NULL_STR = "null";
    }

    @Override
    public Invoice get(Long invId, Boolean isShowDetail) {
        return selectJoinOne(Invoice.class, getWrapper(invId, null,
                null, null,
                null, null, null, null, isShowDetail));
    }

    @Override
    public List<Invoice> searchNormalAndRedBySale(List<Long> invoiceDates) {
        return selectJoinList(Invoice.class, getWrapper(null, InvCateEnum.OUT.getCode(),
                Collections.emptyList(), invoiceDates,
                null, null, null, null, null)
                .in(Invoice::getInvoiceStatus,
                        Arrays.asList(InvStatusEnum.NORMAL.getCode(), InvStatusEnum.RED.getCode())));
    }

    @Override
    public List<Invoice> searchAuthByInItem(List<Long> invoiceDates) {
        return selectJoinList(Invoice.class, getWrapper(null, InvCateEnum.IN.getCode(),
                Collections.emptyList(), invoiceDates,
                null, null, null, null, null)
                .eq(Invoice::getIsAuth, Boolean.TRUE));
    }

    @Override
    public List<Invoice> searchFeesInvoice(List<Long> invoiceDates) {
        return selectJoinList(Invoice.class, getWrapper(null, InvCateEnum.FEES.getCode(),
                Collections.emptyList(), invoiceDates,
                null, null, null, null, null));
    }

    @Override
    public List<Invoice> search(Integer invoiceCate, List<Long> mateDates, List<Long> invoiceDates, String remark, String theInvoiceRead, String invoiceImageName, String commodityName, Boolean isShowDetail, List<Long> idList) {
        return selectJoinList(Invoice.class, getWrapper(null, invoiceCate, mateDates, invoiceDates,
                remark, theInvoiceRead, invoiceImageName, commodityName, isShowDetail)
                .in(!ObjectUtils.isEmpty(idList), Invoice::getId, idList)
        );
    }

    @Override
    public Page<Invoice> search(Integer current, Integer size, Integer invoiceCate, List<Long> mateDates, List<Long> invoiceDates, String remark, String theInvoiceRead, String invoiceImageName, String commodityName, Boolean isShowDetail) {
        return selectJoinListPage(new Page<>(current, size), Invoice.class,
                getWrapper(null, invoiceCate, mateDates, invoiceDates,
                        remark, theInvoiceRead, invoiceImageName, commodityName, isShowDetail));
    }

    /**
     * @param invId            发票id
     * @param invoiceCate      发票分类：0.销项发票;1.进项发票;2.费用小票;
     * @param mateDates        制单日期时间戳列表
     * @param invoiceDates     发票日期时间戳列表
     * @param remark           备注
     * @param theInvoiceRead   本批发票说明
     * @param invoiceImageName 发票影像名称
     * @param commodityName    商品名称
     * @param isShowDetail     是否显示发票明细
     */
    private MPJLambdaWrapper<Invoice> getWrapper(Long invId, Integer invoiceCate,
                                                 List<Long> mateDates, List<Long> invoiceDates,
                                                 String remark, String theInvoiceRead, String invoiceImageName, String commodityName,
                                                 Boolean isShowDetail) {
        return new MPJLambdaWrapper<Invoice>()
                .selectAll(Invoice.class)

                .selectCollection(InvoiceDetail.class, Invoice::getDetails)
                .selectAssociation(Certificate.class, Invoice::getCertificate)
                .leftJoin(InvoiceDetail.class, InvoiceDetail::getInvoiceId, Invoice::getId)
                .leftJoin(Certificate.class, Certificate::getId, Invoice::getCertificateId)

                .eq(Invoice::getAccountingSetId, LoginUser.getLoginSetId())
                .eq(!ObjectUtils.isEmpty(invId), Invoice::getId, invId)
                .eq(!ObjectUtils.isEmpty(invoiceCate), Invoice::getInvoiceCategory, invoiceCate)

                //费用小票没有录入发票明细的区分，所以表达式排除它。
                .eq(Objects.nonNull(isShowDetail) && ObjectUtils.isEmpty(invId) && (invoiceCate < NumberUtils.INTEGER_TWO),
                        Invoice::getIsInvoiceDetail,
                        Objects.nonNull(isShowDetail) && isShowDetail ? NumberUtils.INTEGER_ONE : NumberUtils.INTEGER_ZERO)

                //制单日期查询条件
                .and(!ObjectUtils.isEmpty(mateDates), ext -> ext
                        .ge(Invoice::getCreateTime, new Date(mateDates.get(0)))
                        .lt(Invoice::getCreateTime, new Date(mateDates.get(1)))
                )

                //发票日期查询条件
                .and(!ObjectUtils.isEmpty(invoiceDates), ext -> ext
                        .ge(Invoice::getOpenDate, new Date(invoiceDates.get(0)))
                        .lt(Invoice::getOpenDate, new Date(invoiceDates.get(1)))
                )

                .like(StringUtils.isNotBlank(remark), Invoice::getRemark, remark)
                .like(StringUtils.isNotBlank(commodityName) && isShowDetail, InvoiceDetail::getName, commodityName)

                .orderByAsc(Invoice::getCreateTime);
    }

    @Override
    public Page<CertificateTemplate> searchTemp(Integer current, Integer size, Integer invoiceCategory, Boolean isActive, String name) {
        Page<Long> tempIdPage = getTempIdPage(current, size, invoiceCategory, isActive, name);
        if (tempIdPage.getRecords().isEmpty())
            return new Page<>();

        List<CertificateTemplate> certTempList = listCertTemp(tempIdPage.getRecords());

        return new Page<CertificateTemplate>()
                .setCurrent(tempIdPage.getCurrent())
                .setSize(tempIdPage.getSize())
                .setTotal(tempIdPage.getTotal())
                .setRecords(certTempList);
    }

    /**
     * 获取凭证模板id分页
     *
     * @param current         页码
     * @param size            条数
     * @param invoiceCategory 发票分类：0.销项发票;1.进项发票;2.费用小票;
     * @param isActive        是否启用该项目：1.启用;0.关闭;
     * @param name            模板名称
     */
    private Page<Long> getTempIdPage(Integer current, Integer size, Integer invoiceCategory, Boolean isActive, String name) {
        return certTempORM.selectJoinListPage(new Page<>(current, size), Long.class,
                new MPJLambdaWrapper<CertificateTemplate>()
                        .select(CertificateTemplate::getId)
                        .eq(CertificateTemplate::getInvoiceCategory, invoiceCategory)
                        .eq(CertificateTemplate::getIsActive, isActive)
                        .like(StringUtils.isNotBlank(name) && !name.equals(StringTIP.NULL_STR), CertificateTemplate::getName, name));
    }

    public List<CertificateTemplate> listCertTemp(List<Long> tempIdList) {
        return certTempORM.selectJoinList(CertificateTemplate.class, new MPJLambdaWrapper<CertificateTemplate>()
                .selectAll(CertificateTemplate.class)

                //凭证摘要集合
                .selectCollection(CertificateTemplateAbstract.class, CertificateTemplate::getTemplateAbstractList, collection -> collection
                        .association(PriceType.class, CertificateTemplateAbstract::getPriceType)
                        .association(Account.class, CertificateTemplateAbstract::getAccount)
                )
                .leftJoin(CertificateTemplateAbstract.class, CertificateTemplateAbstract::getTemplateId, CertificateTemplate::getId)

                .leftJoin(PriceType.class, PriceType::getId, CertificateTemplateAbstract::getPriceTypeId)
                .leftJoin(Account.class, Account::getId, CertificateTemplateAbstract::getAccountId)

                .eq(CertificateTemplate::getAccountingSetId, LoginUser.getLoginSetId())
                .in(CertificateTemplate::getId, tempIdList));
    }
}