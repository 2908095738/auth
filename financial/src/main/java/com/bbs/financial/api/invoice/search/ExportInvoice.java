package com.bbs.financial.api.invoice.search;

import cn.hutool.poi.excel.ExcelWriter;
import com.bbs.financial.converter.InvoiceConverter;
import com.bbs.financial.dto.ExcelInvoiceDto;
import com.bbs.financial.dto.InvoiceDto;
import com.bbs.financial.enums.InvCateEnum;
import com.bbs.financial.util.DateUtil;
import com.bbs.financial.util.ExcelUtil;
import com.bbs.financial.util.LoginUser;
import com.bbs.financial.util.SpringUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Controller
@RequestMapping
public class ExportInvoice {
    @Resource
    private ApplicationContext appContext;

    @Resource
    private InvoiceConverter invConverter;

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
    @ResponseBody
    @GetMapping("/invoice/export")
    public void export(HttpServletRequest req, HttpServletResponse resp,
                       @RequestParam(defaultValue = "1") Integer current, @RequestParam(defaultValue = "10") Integer size,
                       @RequestParam Integer invoiceCate,
                       @RequestParam(value = "mateDates", required = false) String mateDatesStr, @RequestParam(name = "invoiceDates", required = false) String invoiceDatesStr,
                       @RequestParam(name = "remark", required = false) String remark, @RequestParam(name = "theInvoiceRead", required = false) String theInvoiceRead,
                       @RequestParam(name = "invoiceImageName", required = false) String invoiceImageName, @RequestParam(name = "commodityName", required = false) String commodityName,
                       @RequestParam(name = "isShowDetail") Boolean isShowDetail) {
        List<InvoiceDto> invList = SpringUtil.getRespData(SearchInvoice.class, appContext,
                s -> s.search(current, size, invoiceCate,
                        mateDatesStr, invoiceDatesStr, remark, theInvoiceRead, invoiceImageName, commodityName, isShowDetail)).getRecords();

        Consumer<ExcelWriter> initFrameFunc = w -> {
            w.merge(0, 0, 0, 2, getMonthRange(mateDatesStr, invoiceDatesStr), false);
            w.merge(0, 0, 3, ExcelInvoiceDto.class.getDeclaredFields().length - 2, LoginUser.get().getName(), false);

            ExcelUtil.setTextCell(w);
            excelMapByInv(w);

            //设置实际输出数据起始行
            w.setCurrentRow(1);
            //未映射实例域不输出
            w.setOnlyAlias(true);
        };

        Supplier<List<ExcelInvoiceDto>> initDataFunc = () -> {
            List<ExcelInvoiceDto> resultList = new ArrayList<>();
            for (InvoiceDto dto : invList)
                resultList.add(invConverter.toDto(dto));

            return resultList;
        };

        ExcelUtil.export(req, resp, "FaPiao", initFrameFunc, initDataFunc);
    }

    /**
     * 获取月份区间字符串
     *
     * @param mateDatesStr    伪制单日期区间时间戳列表字符串
     * @param invoiceDatesStr 伪发票日期区间时间戳列表字符串
     */
    private String getMonthRange(String mateDatesStr, String invoiceDatesStr) {
        List<Long> mateDates = SpringUtil.str2ListByQs(mateDatesStr, Long::valueOf);
        List<Long> invoiceDates = SpringUtil.str2ListByQs(invoiceDatesStr, Long::valueOf);
        String monthRangeStr = null;
        if (!mateDates.isEmpty())
            monthRangeStr = DateUtil.getMonthRange(mateDates.get(NumberUtils.INTEGER_ZERO), mateDates.get(NumberUtils.INTEGER_ONE));
        if (!invoiceDates.isEmpty())
            monthRangeStr = DateUtil.getMonthRange(invoiceDates.get(NumberUtils.INTEGER_ZERO), invoiceDates.get(NumberUtils.INTEGER_ONE));

        return monthRangeStr;
    }

    /**
     * 表格-实体类映射[发票]
     */
    private void excelMapByInv(ExcelWriter writer) {
        writer.addHeaderAlias("openDateStr", "开票日期");
        writer.addHeaderAlias("invoiceTypeStr", "发票类型");
        writer.addHeaderAlias("invoiceCode", "发票代码");
        writer.addHeaderAlias("invoiceNumber", "发票号码");
        writer.addHeaderAlias("clientName", "客户名称");
        writer.addHeaderAlias("nonTaxMoneyStr", "不含税金额");
        writer.addHeaderAlias("taxMoneyStr", "税额");
        writer.addHeaderAlias("taxTotalStr", "价税合计");
        writer.addHeaderAlias("invoiceImage", "发票影像");
        writer.addHeaderAlias("tempName", "凭证模板");
        writer.addHeaderAlias("invoiceStatusStr", "发票状态");
        writer.addHeaderAlias("makeName", "制单人");
        writer.addHeaderAlias("createTimeStr", "创建时间");
        writer.addHeaderAlias("certName", "凭证字号");
        writer.addHeaderAlias("noteDateStr", "记账期间");
        writer.addHeaderAlias("verifyCode", "校验码后六位");
        writer.addHeaderAlias("result", "查验结果");
        writer.addHeaderAlias("theInvoiceRead", "本批发票说明");
    }
}