package com.bbs.financial.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.financial.entity.CertificateTemplate;
import com.bbs.financial.entity.Invoice;
import com.github.yulichang.base.MPJBaseService;

import java.util.List;

/**
 * @author Mafty
 * @description 针对表【invoice(发票)】的数据库操作Service
 * @createDate 2024-06-20 14:36:30
 */
public interface InvoiceService extends MPJBaseService<Invoice> {

    /**
     * 获取当前发票
     *
     * @param invId        发票id
     * @param isShowDetail 是否显示发票明细
     */
    Invoice get(Long invId, Boolean isShowDetail);

    /**
     * 获取销项发票中的发票状态为正常、红冲的发票列表
     *
     * @param invoiceDates 发票日期时间戳列表
     */
    List<Invoice> searchNormalAndRedBySale(List<Long> invoiceDates);

    /**
     * 获取进项发票中的已认证发票列表
     *
     * @param invoiceDates 发票日期时间戳列表
     */
    List<Invoice> searchAuthByInItem(List<Long> invoiceDates);

    /**
     * 获取费用小票
     */
    List<Invoice> searchFeesInvoice(List<Long> invoiceDates);

    /**
     * 获取发票列表
     *
     * @param invoiceCate      发票分类：0.销项发票;1.进项发票;2.费用小票;
     * @param mateDates        制单日期时间戳列表
     * @param invoiceDates     发票日期时间戳列表
     * @param remark           备注
     * @param theInvoiceRead   本批发票说明
     * @param invoiceImageName 发票影像名称
     * @param commodityName    商品名称
     * @param isShowDetail     是否显示发票明细
     * @param idList           发票id列表
     */
    List<Invoice> search(Integer invoiceCate,
                         List<Long> mateDates, List<Long> invoiceDates,
                         String remark, String theInvoiceRead, String invoiceImageName, String commodityName, Boolean isShowDetail, List<Long> idList);

    /**
     * 获取发票列表
     *
     * @param current          页码
     * @param size             条数
     * @param invoiceCate      发票分类：0.销项发票;1.进项发票;2.费用小票;
     * @param mateDates        制单日期时间戳列表
     * @param invoiceDates     发票日期时间戳列表
     * @param remark           备注
     * @param theInvoiceRead   本批发票说明
     * @param invoiceImageName 发票影像名称
     * @param commodityName    商品名称
     * @param isShowDetail     是否显示发票明细
     */
    Page<Invoice> search(Integer current, Integer size, Integer invoiceCate,
                         List<Long> mateDates, List<Long> invoiceDates,
                         String remark, String theInvoiceRead, String invoiceImageName, String commodityName, Boolean isShowDetail);

    /**
     * 查询凭证模板
     *
     * @param current         页码
     * @param size            条数
     * @param invoiceCategory 发票分类：0.销项发票;1.进项发票;2.费用小票;
     * @param isActive        是否启用该项目：1.启用;0.关闭;
     * @param name            模板名称
     */
    Page<CertificateTemplate> searchTemp(Integer current, Integer size, Integer invoiceCategory, Boolean isActive, String name);

    /**
     * 获取凭证模板列表
     *
     * @param tempIdList 凭证模板id列表
     */
    List<CertificateTemplate> listCertTemp(List<Long> tempIdList);
}