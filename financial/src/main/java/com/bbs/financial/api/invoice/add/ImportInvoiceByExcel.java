package com.bbs.financial.api.invoice.add;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.bbs.Result;
import com.bbs.financial.api.invoice.search.SearchAuxByInvoice;
import com.bbs.financial.dto.InvoiceSubjAuxDto;
import com.bbs.financial.entity.Invoice;
import com.bbs.financial.entity.InvoiceDetail;
import com.bbs.financial.enums.InvCateEnum;
import com.bbs.financial.enums.InvStatusEnum;
import com.bbs.financial.enums.InvTypeEnum;
import com.bbs.financial.enums.InvoiceExcelEnum;
import com.bbs.financial.service.InvoiceDetailService;
import com.bbs.financial.service.InvoiceService;
import com.bbs.financial.util.DateUtil;
import com.bbs.financial.util.LoginUser;
import com.bbs.financial.util.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.bbs.financial.enums.InvoiceExcelEnum.*;

/**
 * 表格导入发票
 */
@Slf4j
@RestController
@RequestMapping
public class ImportInvoiceByExcel {

    @Resource
    private DataSourceTransactionManager transactionManager;

    @Resource
    private TransactionDefinition transactionDefinition;

    @Resource
    private ApplicationContext appContext;

    @Resource
    private InvoiceService orm;

    @Resource
    private InvoiceDetailService detailORM;

    private final class StringTIP {
        private StringTIP() {
            throw new RuntimeException("stop create obj");
        }

        public static final String NO_DATA = "表格无数据";
    }

    /**
     * 导入日记账
     *
     * @param excelFile 表格文件
     */
    @PostMapping("/invoice/import/excel")
    public Result<Boolean> importNote(@RequestParam("excelFile") MultipartFile excelFile) {
        Result<List<Map<String, Object>>> excelDataResult = getExcelData(excelFile);
        if (!excelDataResult.getCode().equals(Result.success().getCode()))
            return Result.failed(excelDataResult.getMsg());

        if (ObjectUtils.isEmpty(excelDataResult.getData()))
            return Result.failed(StringTIP.NO_DATA);

        List<Invoice> saveList = getInvList(excelDataResult.getData());

        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            orm.saveBatch(saveList);//保存发票
            saveDatails(saveList);
            transactionManager.commit(transaction);
            return Result.success();
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new RuntimeException(e);
        }
    }

    private Result<List<Map<String, Object>>> getExcelData(MultipartFile file) {
        List<Map<String, Object>> excelData = Collections.emptyList();

        try {
            ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
            reader.setIgnoreEmptyRow(true);

            //如果表格数据异常，大概率是这里参数设置问题
            excelData = reader.read(0, 1, Integer.MAX_VALUE);
        } catch (Exception e) {
            log.error("import fail", e);
            return Result.failed("导入失败");
        }

        return Result.success(excelData);
    }

    /**
     * 获取发票列表
     */
    private List<Invoice> getInvList(List<Map<String, Object>> excelData) {
        List<Invoice> saveList = new ArrayList<>();

        for (int i = 0; i < excelData.size(); i++) {
            Map<String, Object> fieldMap = excelData.get(i);

            Function<InvoiceExcelEnum, String> getFieldFunc = k ->
                    Objects.nonNull(fieldMap.get(k.getName())) ?
                            String.valueOf(fieldMap.get(k.getName())) : "";

            saveList.add(getInv(getFieldFunc));
        }

        return saveList;
    }

    /**
     * 获取发票实例
     *
     * @param getFieldFunc 获取实例域接口
     */
    private Invoice getInv(Function<InvoiceExcelEnum, String> getFieldFunc) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceCode(getFieldFunc.apply(INV_CODE));
        invoice.setOpenDate(DateUtil.getDate(getFieldFunc.apply(OPEN_DATE)));
        invoice.setIsInvoiceDetail(NumberUtils.INTEGER_ONE);
        invoice.setRemark(getFieldFunc.apply(REMARK));
        invoice.setCreateBy(LoginUser.getId());
        invoice.setAccountingSetId(LoginUser.getLoginSetId());

        invoice.setInvoiceStatus(InvStatusEnum.getEnumByMsg(getFieldFunc.apply(INV_STATUS)));

        invoice.setInvoiceType(InvTypeEnum.getEnumByMsg(getFieldFunc.apply(INV_TYPE)));
        setInvNumber(invoice, getFieldFunc);

        setInvCateAndOther(invoice, getFieldFunc);

        invoice.setDetails(Collections.singletonList(getDetail(getFieldFunc)));
        return invoice;
    }

    /**
     * 设置发票号码
     *
     * @param invoice      发票
     * @param getFieldFunc 获取实例域接口
     */
    private void setInvNumber(Invoice invoice, Function<InvoiceExcelEnum, String> getFieldFunc) {
        switch (invoice.getInvoiceType()) {
            case ELE_S:
            case ELE_NORMAL:
                invoice.setInvoiceNumber(getStrByExcel(getFieldFunc, ELE_INV_NUM));
                break;
            default:
                invoice.setInvoiceNumber(getStrByExcel(getFieldFunc, INV_NUM));
                break;
        }
    }

    /**
     * 发票分类、相关实例域赋值
     *
     * @param invoice      发票
     * @param getFieldFunc 获取实例域接口
     */
    private void setInvCateAndOther(Invoice invoice, Function<InvoiceExcelEnum, String> getFieldFunc) {
        String saleName = getStrByExcel(getFieldFunc, SALE_NAME);
        String saleUSCC = getStrByExcel(getFieldFunc, SALE_USCC);
        if (!saleName.isEmpty() && !saleUSCC.isEmpty()) {
            invoice.setClientName(saleName);
            invoice.setCreditCode(saleUSCC);
            invoice.setInvoiceCategory(InvCateEnum.OUT);
            return;
        }

        String buyName = getStrByExcel(getFieldFunc, BUY_NAME);
        String buyUSCC = getStrByExcel(getFieldFunc, BUY_USCC);
        if (!buyName.isEmpty() && !buyUSCC.isEmpty()) {
            invoice.setClientName(buyName);
            invoice.setCreditCode(buyUSCC);
            invoice.setInvoiceCategory(InvCateEnum.IN);
            return;
        }
    }

    /**
     * 从excel获取字符串
     *
     * @param getFieldFunc 获取实例域接口
     * @param invEnum      发票表格实例域枚举
     * @return 无值返回空字符串
     */
    private String getStrByExcel(Function<InvoiceExcelEnum, String> getFieldFunc, InvoiceExcelEnum invEnum) {
        return StringUtils.isNotBlank(getFieldFunc.apply(invEnum)) ? getFieldFunc.apply(invEnum) : "";
    }

    /**
     * 获取发票明细实例
     *
     * @param getFieldFunc 获取实例域接口
     */
    private InvoiceDetail getDetail(Function<InvoiceExcelEnum, String> getFieldFunc) {
        InvoiceDetail detail = new InvoiceDetail();
        detail.setName(getFieldFunc.apply(DETAIL_NAME));
        detail.setCode(getStrByExcel(getFieldFunc, DETAIL_CODE));
        detail.setUnit(getStrByExcel(getFieldFunc, UNIT));

        detail.setQuantity(Integer.valueOf(getFieldFunc.apply(QUANTITY)));
        detail.setPrice(new BigDecimal(getFieldFunc.apply(PRICE)));
        detail.setNonTaxMoney(new BigDecimal(getFieldFunc.apply(NON_TAX_MONEY)));
        detail.setTaxRates(new BigDecimal(getFieldFunc.apply(TAX)));
        detail.setTaxMoney(new BigDecimal(getFieldFunc.apply(TAX)));

        return detail;
    }

    /**
     * 保存发票明细
     *
     * @param saveList 发票列表
     */
    public void saveDatails(List<Invoice> saveList) {
        //辅助核算id和辅助核算名称的映射
        Map<String, Long> auxIdByAuxName = SpringUtil.
                getRespData(SearchAuxByInvoice.class, appContext, s -> s.search(NumberUtils.INTEGER_ONE, 8192))
                .getRecords()
                .stream()
                .collect(Collectors.toMap(InvoiceSubjAuxDto::getName, InvoiceSubjAuxDto::getId));

        for (Invoice inv : saveList) {
            List<InvoiceDetail> detailList = inv.getDetails();
            for (InvoiceDetail detail : detailList) {
                detail.setInvoiceId(inv.getId());

                Long auxId = auxIdByAuxName.get(detail.getName());
                detail.setName(Objects.nonNull(auxId) ? detail.getName() : null);
                detail.setAbstAuxId(auxId);
            }
        }

        detailORM.saveBatch(saveList.stream().map(Invoice::getDetails).flatMap(List::stream).collect(Collectors.toList()));
    }
}