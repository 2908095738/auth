package com.bbs.financial.api.invoice.search;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.api.auth.User;
import com.bbs.api.auth.UserAPI;
import com.bbs.financial.converter.InvoiceConverter;
import com.bbs.financial.dto.InvoiceDto;
import com.bbs.financial.entity.*;
import com.bbs.financial.enums.InvCateEnum;
import com.bbs.financial.service.InvoiceService;
import com.bbs.financial.util.SpringUtil;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping
public class SearchInvoice {
    @Resource
    private InvoiceService invoiceService;

    @Resource
    private InvoiceConverter invConverter;

    @DubboReference
    private UserAPI userAPI;

    private static class StringTIP {
        public static final String RESET_AUX = "请选择";
    }

    /**
     * 获取发票详情
     *
     * @param id           发票id
     * @param isShowDetail 是否显示发票明细
     */
    @GetMapping("/invoice/{id}/{isShowDetail}")
    public Result<InvoiceDto> search(@PathVariable Long id, @PathVariable Boolean isShowDetail) {
        Invoice invoice = invoiceService.get(id, isShowDetail);
        return Result.success(invConverter.toDto(invoice));
    }

    /**
     * 获取发票列表
     *
     * @param current          页码
     * @param size             条数
     * @param invoiceCate      发票分类：{@link InvCateEnum}
     * @param mateDatesStr     伪制单日期区间时间戳列表字符串
     * @param invoiceDatesStr  伪发票日期区间时间戳列表字符串
     * @param remark           备注
     * @param theInvoiceRead   本批发票说明
     * @param invoiceImageName 发票影像名称
     * @param commodityName    商品名称
     * @param isShowDetail     是否显示发票明细
     */
    @GetMapping("/invoice/list")
    public Result<Page<InvoiceDto>> search(
            @RequestParam(defaultValue = "1") Integer current, @RequestParam(defaultValue = "10") Integer size,
            @RequestParam Integer invoiceCate,
            @RequestParam(value = "mateDates", required = false) String mateDatesStr, @RequestParam(name = "invoiceDates", required = false) String invoiceDatesStr,
            @RequestParam(name = "remark", required = false) String remark, @RequestParam(name = "theInvoiceRead", required = false) String theInvoiceRead,
            @RequestParam(name = "invoiceImageName", required = false) String invoiceImageName, @RequestParam(name = "commodityName", required = false) String commodityName,
            @RequestParam(name = "isShowDetail") Boolean isShowDetail
    ) {
        //获取原始数据
        List<Long> mateDates = SpringUtil.str2ListByQs(mateDatesStr, Long::valueOf);
        List<Long> invoiceDates = SpringUtil.str2ListByQs(invoiceDatesStr, Long::valueOf);
        Page<Invoice> tmpPage = invoiceService.search(current, size, invoiceCate, mateDates, invoiceDates,
                remark, theInvoiceRead, invoiceImageName, commodityName, isShowDetail);

        List<InvoiceDto> resultList = initFieldByInvoiceList(tmpPage.getRecords());

        //重置分页
        return Result.success(new Page<InvoiceDto>()
                .setCurrent(current)
                .setSize(size)
                .setTotal(tmpPage.getTotal())
                .setRecords(resultList)
        );
    }

    /**
     * 初始化发票实例域列表
     *
     * @param invList 发票列表
     */
    private List<InvoiceDto> initFieldByInvoiceList(List<Invoice> invList) {
        //初始化用户
        fillUser(invList, searchIdUserMap(filterUserIds(invList)));
        //制单人跟发票的映射
        Map<Long, Invoice> invByInvId = invList.stream().collect(Collectors.toMap(Invoice::getId, i -> i));

        //初始化其他实例域
        List<InvoiceDto> doneList = invList.stream().map(invConverter::toDto).collect(Collectors.toList());
        doneList.forEach(d -> initField(d, invByInvId));

        return doneList;
    }

    /**
     * @param invoiceCate  发票分类：{@link InvCateEnum}
     * @param isShowDetail 是否显示发票明细
     * @param idList       发票id列表
     */
    public List<InvoiceDto> search(Integer invoiceCate, Boolean isShowDetail, List<Long> idList) {
        List<Invoice> tmpList = invoiceService.search(invoiceCate, null, null,
                null, null, null, null, isShowDetail, idList);

        return initFieldByInvoiceList(tmpList);
    }

    private void fillUser(List<Invoice> invList, Map<Long, User> map) {
        invList.forEach(note -> note.setCreateUser(map.get(note.getCreateBy())));
    }

    private Map<Long, User> searchIdUserMap(Set<Long> userIds) {
        return userAPI.getUserList(userIds).stream().collect(Collectors.toMap(User::getId, user -> user));
    }

    private Set<Long> filterUserIds(List<Invoice> invList) {
        Set<Long> userIds = new HashSet<>();
        invList.forEach(i -> userIds.add(i.getCreateBy()));
        return userIds;
    }

    /**
     * 初始化发票实例域
     *
     * @param dto        发票数据
     * @param invByInvId 制单人跟发票的映射
     */
    private void initField(InvoiceDto dto, Map<Long, Invoice> invByInvId) {
        List<InvoiceDetail> detailList = dto.getDetails();

        //金额、发票明细名称赋值
        BigDecimal nonTax = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        for (InvoiceDetail detail : detailList) {
            //导入表格时，不存在相应辅助核算，则重新赋值，让用户重新选择。
            if (ObjectUtils.isEmpty(detail.getAbstAuxId()))
                detail.setName(StringTIP.RESET_AUX);

            nonTax = nonTax.add(detail.getNonTaxMoney());
            tax = tax.add(Objects.nonNull(detail.getTaxMoney()) ? detail.getTaxMoney() : new BigDecimal("0.00"));
        }
        dto.setNonTaxMoney(nonTax);
        dto.setTaxMoney(tax);

        Invoice invoice = invByInvId.get(dto.getId());
        //凭证字号、记账期间赋值
        Certificate cert = invoice.getCertificate();
        if (!ObjectUtils.isEmpty(cert)) {
            dto.setCertName(cert.getCertificateWord().getMsg() + "-" + cert.getNo());

            SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
            dto.setNoteDateStr(format.format(cert.getDate()));
        }

        //制单人赋值
        dto.setMakeName(invoice.getCreateUser().getName());
    }
}