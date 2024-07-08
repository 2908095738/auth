package com.bbs.financial.api.note.importNote;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.bbs.Result;
import com.bbs.financial.entity.Account;
import com.bbs.financial.entity.Note;
import com.bbs.financial.enums.CashierExeclEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.bbs.financial.enums.CashierExeclEnum.*;

@Slf4j
@RestController
@RequestMapping
public class ImportNote {

    private final class StringTIP {
        private StringTIP() {
            throw new RuntimeException("stop create obj");
        }

        public static final String SPACE = " ";
    }

    /**
     * 导入日记账
     *
     * @param file 文件
     * @param zhId 账户id
     */
    @PostMapping("/import/note/{zhId}")
    public Result<Boolean> importNote(@RequestParam("file") MultipartFile file, @RequestParam("zhId") Long zhId) {
        Result<List<Map<String, Object>>> excelDataResult = getExcelData(file);
        if (excelDataResult.getCode() != Result.success().getCode())
            return Result.failed(excelDataResult.getMsg());

        if (ObjectUtils.isEmpty(excelDataResult.getData()))
            return Result.failed("表格无数据");

        List<Note> noteList = new ArrayList<>();
        initNoteList(excelDataResult.getData(), noteList);

        return Result.success(Boolean.FALSE);
    }

    /**
     * 获取表格数据
     */
    private Result<List<Map<String, Object>>> getExcelData(MultipartFile file) {
        List<Map<String, Object>> excelData = null;

        try {
            ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
            reader.setIgnoreEmptyRow(true);

            //TODO L 如果表格数据异常，大概率是这里参数设置问题
            excelData = reader.read(0, 1, Integer.MAX_VALUE);
        } catch (Exception e) {
            log.error("导入失败", e);
            return Result.failed("导入失败");
        }

        return Result.success(excelData);
    }

    /**
     * 初始化日记账列表
     */
    private void initNoteList(List<Map<String, Object>> excelData, List<Note> noteList) {

        for (int i = 0; i < excelData.size(); i++) {
            Map<String, Object> noteMap = excelData.get(i);

            Function<CashierExeclEnum, String> fieldFunc = k -> (String) noteMap.get(k.getValue());

            noteList.add(getNoteByRow(fieldFunc));
        }
    }

    /**
     * 获取日记账的日期
     *
     * @param dateStr 日期字符串[YYYY-MM-DD]
     * @return
     */
    private Date getDateByNote(String dateStr) {
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate localDate = LocalDate.parse(dateStr);
        ZonedDateTime zdt = localDate.atStartOfDay(zoneId);
        return Date.from(zdt.toInstant());
    }

    /**
     * 从表格的一行数据中获取日记账
     *
     * @param fieldFunc 实例域获取接口
     */
    private Note getNoteByRow(Function<CashierExeclEnum, String> fieldFunc) {
        Note note = new Note();
        note.setDate(getDateByNote(fieldFunc.apply(DATE)));
        note.setCertificateAbstract(fieldFunc.apply(CERT_ABS));

        note.setHeAccount(getSubjQueryByRow(fieldFunc.apply(HE_ACCOUNT)));

        note.setBorrowMoney(new BigDecimal(fieldFunc.apply(BORROW)));
        note.setLoansMoney(new BigDecimal(fieldFunc.apply(LOANS)));
        note.setRemark(fieldFunc.apply(REMARK));
        return note;
    }

    /***
     * 从表格的一行数据中获取科目搜索条件
     * @param subjQuery 科目查询条件耦合字符串
     */
    private Account getSubjQueryByRow(String subjQuery) {
        if (StringUtils.isBlank(subjQuery))
            return null;

        String[] subjPart = subjQuery.split(StringTIP.SPACE);//分割后的科目查询条件
        Account account = new Account();
        account.setNo(subjPart[0]);
        account.setName(subjPart[1]);
        return account;
    }
}