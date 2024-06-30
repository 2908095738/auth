package com.bbs.financial.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import cn.hutool.poi.excel.StyleSet;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.api.auth.User;
import com.bbs.api.auth.UserAPI;
import com.bbs.financial.dto.*;
import com.bbs.financial.entity.*;
import com.bbs.financial.service.*;
import com.bbs.financial.util.LoginUser;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.dubbo.common.utils.Holder;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.bbs.Result.success;
import static java.util.Objects.nonNull;
import static com.bbs.financial.util.ExcelUtil.setResponseHeader;
import static org.apache.commons.lang3.math.NumberUtils.*;

/**
 * 出纳控制器
 */
@Slf4j
@RestController
@RequestMapping("/cashier")
public class CashierController {

    @DubboReference
    private UserAPI userAPI;

    @Resource
    private AccountService accountService;

    @Resource
    private CertificateService certificateService;

    @Resource
    private ZhangHuService zhService;

    @Resource
    private PriceTypeService priceTypeService;

    @Resource
    private CashierService cashierService;

    @Resource
    private CertificateAbstractService certificateAbstractService;

    /**
     * 查询日记账
     *
     * @param current   页码
     * @param size      条数
     * @param zhangHuId 账户id
     * @param startDateLong 起始时间时间戳
     * @param endDateLong   结束时间时间戳
     */
    @GetMapping("/listCertificate")
    public Result<Page<Certificate>> listCertificate(
            @RequestParam(defaultValue = "1") Integer current, @RequestParam(defaultValue = "10") Integer size,
            @RequestParam Long zhangHuId,
            @RequestParam(name = "startDate", required = false) Long startDateLong,
            @RequestParam(name = "endDate", required = false) Long endDateLong) {

        //获取凭证分页
        Page<Certificate> certificatePage = cashierService.listCertificate(current, size, LoginUser.getCompanyId(), zhangHuId, null, startDateLong, endDateLong, Boolean.TRUE, Boolean.TRUE);

        //凭证摘要列表排序
        sortByAbstList(certificatePage.getRecords());

        Long oriMoney = getOriMoney(zhangHuId, String.valueOf(startDateLong)).getData();
        initLessMoney(certificatePage.getRecords(), oriMoney);

        // 获取【创建用户】&&【审核用户】的 userId Set
        Set<Long> userIds = filterUserIds(certificatePage);
        // 查询用户信息
        Map<Long, User> map = searchIdUserMap(userIds);
        // 回填用户信息
        fillUser(certificatePage, map);

        return Result.success(certificatePage);
    }

    /**
     * 对凭证摘要排序
     *
     * @param list 凭证列表
     */
    private void sortByAbstList(List<Certificate> list) {
        list.forEach(c ->
                c.getAbstracts().sort((l, r) -> {
                    Long lId = l.getId();
                    Long rId = r.getId();

                    return lId.compareTo(rId);
                }));
    }

    /**
     * 初始化凭证摘要余额
     *
     * @param certList 凭证列表
     * @param oriMoney 期初余额
     */
    private void initLessMoney(List<Certificate> certList, Long oriMoney) {
        BigDecimal nowLess = new BigDecimal(oriMoney);

        for (Certificate cert : certList) {
            List<CertificateAbstract> abstractList = cert.getAbstracts();
            for (CertificateAbstract abst : abstractList) {
                if (!ObjectUtils.isEmpty(abst.getBorrowMoney()) && abst.getBorrowMoney().doubleValue() != 0)
                    nowLess = nowLess.add(BigDecimal.valueOf(abst.getBorrowMoney()));
                else if (!ObjectUtils.isEmpty(abst.getLoansMoney()) && abst.getLoansMoney().doubleValue() != 0)
                    nowLess = nowLess.subtract(BigDecimal.valueOf(abst.getLoansMoney()));
                abst.setSurplusMoney(nowLess.longValue());
            }
        }

    }

    private Set<Long> filterUserIds(Page<Certificate> certificatePage) {
        Set<Long> userIds = new HashSet<>();
        certificatePage.getRecords().forEach(certificate -> {
            userIds.add(certificate.getCreateBy());
            Long authUserId = certificate.getAuthBy();
            if (nonNull(authUserId)) userIds.add(authUserId);
        });
        return userIds;
    }

    private Map<Long, User> searchIdUserMap(Set<Long> userIds) {
        return userAPI.getUserList(userIds).stream().collect(Collectors.toMap(User::getId, user -> user));
    }

    private void fillUser(Page<Certificate> certificatePage, Map<Long, User> map) {
        certificatePage.getRecords().forEach(certificate -> {
            certificate.setCreateUser(map.get(certificate.getCreateBy()));
            Long authUserId = certificate.getAuthBy();
            if (nonNull(authUserId)) certificate.setAuthUser(map.get(certificate.getAuthBy()));
        });
    }

    /**
     * 查询入账科目列表
     */
    @GetMapping("/listSubjects")
    public Result<Page<SubjectsNameDto>> listSubjects(Long companyId, @RequestParam Integer current, @RequestParam Integer size) {
        Page<SubjectsNameDto> page = accountService.selectJoinListPage(new Page<>(current, size), SubjectsNameDto.class, new MPJLambdaWrapper<Account>()
                .select(Account::getId, Account::getNo, Account::getName)
                .eq(Account::getCompanyId, INTEGER_ZERO)
                .or().eq(nonNull(companyId), Account::getCompanyId, companyId)
        );
        // 根据 no 中的 - 的数量，获取需要查询的科目 level
        // ps: value 的三元，可忽略，用于解决 IDEA Null 检查
        // ps:（无具体作用，该判断是否生效取决于 eq 的 isSearchNo; 如果生效，value 始终为 split.length - INTEGER_ONE）

        return success(page);
    }

    /**
     * 获取期初余额
     *
     * @param zhangHuId 账户id
     * @param date      时间戳字符串
     */
    @GetMapping("/cert/oriMoney")
    public Result<Long> getOriMoney( Long zhangHuId, @RequestParam(name = "date", required = false) String date) {
        //计算期初余额
        List<Certificate> tmpList = cashierService.listCertificate(INTEGER_ZERO, INTEGER_ZERO, LoginUser.getCompanyId(), zhangHuId, date, null, null, Boolean.FALSE, Boolean.FALSE).getRecords();
        long oriMoeny = 0L;
        for (Certificate cert : tmpList) {
            List<CertificateAbstract> abstList = cert.getAbstracts();
            if (!ObjectUtils.isEmpty(abstList)) {
                for (CertificateAbstract abst : abstList) {
                    if (!ObjectUtils.isEmpty(abst.getBorrowMoney()))
                        oriMoeny = oriMoeny + abst.getBorrowMoney();
                    if (!ObjectUtils.isEmpty(abst.getLoansMoney()))
                        oriMoeny = oriMoeny - abst.getLoansMoney();

                }
            }
        }

        return Result.success(oriMoeny);
    }

    /**
     * 导出凭证
     *
     * @param resp      响应
     * @param zhangHuId 账户id
     * @param startDateLong 起始时间时间戳
     * @param endDateLong   结束时间时间戳
     */
    @GetMapping("/exportCert")
    public void exportCert(HttpServletRequest req, HttpServletResponse resp,
                           @RequestParam(defaultValue = "1") Integer current, @RequestParam(defaultValue = "10") Integer size,
                            @RequestParam Long zhangHuId,
                           @RequestParam(name = "startDate", required = false) Long startDateLong,
                           @RequestParam(name = "endDate", required = false) Long endDateLong) {
        List<Certificate> oriData = listCertificate(current, size, zhangHuId, startDateLong, endDateLong).getData().getRecords();

        Long oriMoney = getOriMoney(LoginUser.getCompanyId(), String.valueOf(startDateLong)).getData();
        Function<List<Certificate>, List<ExcelNoteDto>> initExcelDataFunc = d -> {
            List<ExcelNoteDto> datas = getExcelDatasByNote(d, oriMoney);
            initZhByNote(datas, LoginUser.getCompanyId());
            return datas;
        };

        Consumer<ExcelWriter> initSubTitleFunc = w -> initSubTitleByNote(w, LoginUser.get().getName(),
                getZhByNote(zhangHuId), getMonthRange(startDateLong, endDateLong));

        exportCore(req, resp, oriData, initExcelDataFunc, 7, "RiJiZhang", "日记账", this::excelMapByNote, initSubTitleFunc);
    }

    /**
     * 导出
     *
     * @param oriData           原始表格数据列表
     * @param initExcelDataFunc 初始化表格数据接口
     * @param titileWidth       标题所占单元格长度
     * @param excelName         表格名称
     * @param excelTitle        表格标题
     * @param excelMapFunc      表格-实体类映射接口
     * @param initSubTitleFunc  初始化子标题接口
     * @param <T>               原始表格数据
     * @param <R>               最终表格数据
     */
    private <T, R> void exportCore(HttpServletRequest req, HttpServletResponse resp,
                                   List<T> oriData, Function<List<T>, List<R>> initExcelDataFunc,
                                   int titileWidth, String excelName, String excelTitle, Consumer<ExcelWriter> excelMapFunc, Consumer<ExcelWriter> initSubTitleFunc) {
        OutputStream out = null;
        ExcelWriter writer = ExcelUtil.getWriter(new String(getExcelName("export").getBytes(StandardCharsets.UTF_8)));
        try {
            out = resp.getOutputStream();
            setResponseHeader(req, resp, getExcelName(excelName));

            initExcel(writer, titileWidth, excelTitle, initSubTitleFunc, excelMapFunc, initExcelDataFunc.apply(oriData));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            writer.flush(out, true);
            writer.close();
            IoUtil.close(out);
        }
    }

    private String getExcelName(String title) {
        DateTime now = DateTime.now();
        return String.format("%d%d%d_%d%d%d_%s.xlsx",
                now.year(), now.monthBaseOne(), now.dayOfMonth(),
                now.hour(true), now.minute(), now.second(),
                title
        );
    }

    /**
     * 获取表格数据
     *
     * @param certList 凭证列表
     * @param oriMoney 期初余额
     */
    private List<ExcelNoteDto> getExcelDatasByNote(List<Certificate> certList, Long oriMoney) {
        List<ExcelNoteDto> result = new ArrayList<>();

        //当前余额
        BigDecimal nowLess = new BigDecimal(oriMoney);

        for (Certificate cert : certList) {
            //可重用实例域值
            String dateStr = DateTime.of(cert.getDate()).toDateStr();
            String certStr = cert.getCertificateWord().getMsg() + cert.getNo();

            String makeName = cert.getCreateUser().getName();

            List<CertificateAbstract> abstList = cert.getAbstracts();
            for (CertificateAbstract abst : abstList) {
                ExcelNoteDto obj = getPartDataByNote(dateStr, certStr, makeName, abst);

                //部分金额赋值
                initMoneyByNow(Collections.singletonList(obj));
                initMoneyByOne(Collections.singletonList(abst), obj, true);

                //余额赋值
                if (obj.getBorrowMoney().doubleValue() != 0)
                    nowLess = nowLess.add(obj.getBorrowMoney());
                else if (obj.getLoansMoney().doubleValue() != 0)
                    nowLess = nowLess.subtract(obj.getLoansMoney());
                obj.setLessMoney(nowLess);

                result.add(obj);
            }
        }

        initFlagNoteByExcel(result, oriMoney);

        return result;
    }

    /**
     * 获取日记账表格数据[可直接赋值的实例域]
     *
     * @param dateStr  日期字符串
     * @param certStr  凭证
     * @param makeName 制单人
     * @param abst     凭证摘要列表
     */
    private ExcelNoteDto getPartDataByNote(String dateStr, String certStr, String makeName, CertificateAbstract abst) {
        ExcelNoteDto obj = new ExcelNoteDto();
        obj.setDateStr(dateStr);
        obj.setCert(certStr);
        obj.setMakeName(makeName);

        obj.setCertificateAbstract(abst.getCertificateAbstract());
        obj.setSubjId(abst.getAccountId());

        return obj;
    }

    /**
     * 初始化起始、结束日记账标识符
     *
     * @param dataList 日记账表格数据列表
     * @param oriMoney 期初余额
     */
    private void initFlagNoteByExcel(List<ExcelNoteDto> dataList, Long oriMoney) {
        //合计数据计算
        BigDecimal borrowTotal = dataList.stream().map(ExcelNoteDto::getBorrowMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal loansTotal = dataList.stream().map(ExcelNoteDto::getLoansMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal lessTotal = borrowTotal.subtract(loansTotal);

        //合计数据赋值
        ExcelNoteDto endTotal = new ExcelNoteDto();
        endTotal.setBorrowMoney(borrowTotal);
        endTotal.setLoansMoney(loansTotal);
        endTotal.setLessMoney(lessTotal);
        endTotal.setCertificateAbstract("合计");

        dataList.add(endTotal);

        //起始标识符赋值
        ExcelNoteDto startData = new ExcelNoteDto();
        startData.setCertificateAbstract("初始余额");
        startData.setLessMoney(BigDecimal.valueOf(oriMoney));
        dataList.add(0, startData);
    }

    /**
     * 获取当月起始、结束日
     *
     * @param startDateLong 起始时间时间戳
     * @param endDateLong   结束时间时间戳
     */
    private static String getMonthRange(Long startDateLong, Long endDateLong) {
        DateTime leftMonth = DateUtil.date(startDateLong);
        DateTime rightMonth = DateUtil.date(endDateLong);
        return String.format("%s 至 %s", leftMonth.toDateStr(), rightMonth.toDateStr());
    }

    /**
     * 日记账表格数据列表的账户账户赋值
     *
     * @param dataList  日记账表格数据列表
     * @param companyId 公司id
     */
    private void initZhByNote(List<ExcelNoteDto> dataList, Long companyId) {
        List<Long> subjIdList = new ArrayList<>();
        for (ExcelNoteDto excelNoteDto : dataList) {
            Long subjId = excelNoteDto.getSubjId();
            if (!ObjectUtils.isEmpty(subjId)) {
                subjIdList.add(subjId);
            }
        }

        if (subjIdList.size() == INTEGER_ZERO)
            return;

        //TODO L 可能存在的map-key重复报错源头
        List<ZhangHu> tmpList = zhService.selectJoinList(ZhangHu.class,
                new MPJLambdaWrapper<ZhangHu>()
                        .select(ZhangHu::getSubjectsId, ZhangHu::getZhangHuCode)
                        .eq(ZhangHu::getCompanyId, companyId)
                        .in(ZhangHu::getSubjectsId, subjIdList)
        );

        //科目id-账户账号映射
        Map<Long, String> zhCodeBySubjId = new HashMap<>();
        tmpList.forEach(z -> zhCodeBySubjId.put(z.getSubjectsId(), z.getZhangHuCode()));

        //实例域赋值
        dataList.forEach(n -> {
            String zhCode = zhCodeBySubjId.get(n.getSubjId());
            n.setHeZhCode(zhCode);
        });
    }

    /**
     * 获取日记账表格标题的账户信息
     *
     * @param zhId 账户id
     */
    private String getZhByNote(Long zhId) {
        if (ObjectUtils.isEmpty(zhId) || (zhId.equals(LONG_ZERO))) {
            return "全部账户";
        }

        ZhangHu zhangHu = zhService.selectJoinOne(ZhangHu.class,
                new MPJLambdaWrapper<ZhangHu>()
                        .select(ZhangHu::getZhangHuCode, ZhangHu::getName)
                        .eq(ZhangHu::getId, zhId)
        );

        StringBuilder zhBuilder = new StringBuilder();
        boolean isCode = !ObjectUtils.isEmpty(zhangHu.getZhangHuCode());
        boolean isName = !ObjectUtils.isEmpty(zhangHu.getName());
        if (isCode && isName)
            zhBuilder.append(zhangHu.getZhangHuCode()).append(" - ");
        if (isName)
            zhBuilder.append(zhangHu.getName());

        return zhBuilder.toString();
    }

    /**
     * 表格-实体类映射[日记账]
     */
    private void excelMapByNote(ExcelWriter writer) {
        writer.addHeaderAlias("dateStr", "日期");
        writer.addHeaderAlias("certificateAbstract", "摘要");
        writer.addHeaderAlias("heZhCode", "对方账户");
        writer.addHeaderAlias("borrowMoney", "收入");
        writer.addHeaderAlias("loansMoney", "支出");
        writer.addHeaderAlias("lessMoney", "余额");
        writer.addHeaderAlias("cert", "凭证");
        writer.addHeaderAlias("makeName", "制单人");
    }

    /**
     * 初始化[日记账]的表格子标题
     *
     * @param userName     用户名
     * @param zhName       账户名称
     * @param dateRangeStr 当月起始、结束日区间字符串
     */
    private void initSubTitleByNote(ExcelWriter writer, String userName, String zhName, String dateRangeStr) {
        writer.merge(1, 1, 0, 2, userName, false);
        writer.merge(1, 1, 3, 5, zhName, false);
        writer.merge(1, 1, 6, 7, dateRangeStr, false);
    }

    /**
     * 初始化表格
     *
     * @param titleWidth       标题所占单元格长度
     * @param initSubTitleFunc 初始化表格子标题
     * @param excelTitle       表格标题
     * @param excelMapFunc     表格-实体类映射接口
     * @param excelDatas       表格数据列表
     * @
     */
    private <T> void initExcel(ExcelWriter writer, int titleWidth,
                               String excelTitle, Consumer<ExcelWriter> initSubTitleFunc, Consumer<ExcelWriter> excelMapFunc, List<T> excelDatas) {
        //标题
        writer.merge(0, 0, 0, titleWidth, excelTitle, false);
        initSubTitleFunc.accept(writer);

        //当月起始、结束表格宽度
        writer.setColumnWidth(0, 14);
        writer.setColumnWidth(1, 16);
        writer.setColumnWidth(6, 13);
        writer.setColumnWidth(7, 11);

        //设置实际输出数据起始行
        writer.setCurrentRow(2);

        excelMapFunc.accept(writer);
        //未映射实例域不输出
        writer.setOnlyAlias(true);

        writer.write(excelDatas, true);

        //获取整个Excel的样式，设置单元格格式为文本
        StyleSet styleSet = writer.getStyleSet();
        CellStyle cellStyle = styleSet.getCellStyleForNumber();
        DataFormat format = writer.getWorkbook().createDataFormat();
        cellStyle.setDataFormat(format.getFormat("@"));
        writer.setStyleSet(styleSet);
    }

    /**
     * 是否可以删除凭证摘要，若对应凭证有多个凭证摘要，则不允删除。
     *
     * @param certId 凭证id
     * @return true: 可以删除;false: 不能删除
     */
    @GetMapping("/certAbst/{certId}")
    public Result<Boolean> isDelAbst(@PathVariable Long certId) {
        //TODO L 因为删除凭证摘要可能会导致[借贷不相等]，所以把[录凭证].save()里的[借贷不相等]算法移植到后端

        return Result.success(Boolean.TRUE);
    }

    /**
     * 删除记录凭证摘要
     *
     * @param companyId 公司id
     * @param msecStr   时间戳字符串
     */
    @DeleteMapping("/certAbst/{companyId}/{msecStr}/{ids}")
    public Result<Boolean> removeAbst(@PathVariable Long companyId, @PathVariable String msecStr, @PathVariable List<Long> ids) {

        //TODO L 暂不使用考虑删除

        //凭证id列表
        List<Long> certIds = certificateAbstractService.selectJoinList(Long.class,
                new MPJLambdaWrapper<CertificateAbstract>()
                        .select(CertificateAbstract::getCertificateId)
                        .in(CertificateAbstract::getId, ids)
        );

        //凭证列表
        List<Certificate> certList = null;
//                cashierService.listCertificate(INTEGER_ZERO, INTEGER_ZERO, companyId, LONG_ZERO, msecStr, Boolean.TRUE, Boolean.FALSE).getRecords();

        //待删除凭证
        certIds.clear();
        certList.forEach(c -> {
            List<CertificateAbstract> abstList = c.getAbstracts();
            boolean isOne = abstList.size() == INTEGER_ONE;
            if (isOne)//只有一条凭证摘要，说明这条摘要就是当前要删的摘要，则连带删除凭证本身。
                certIds.add(c.getId());
        });
        if (certIds.size() > 0)
            certificateService.getBaseMapper().deleteBatchIds(certIds);

        certificateAbstractService.getBaseMapper().deleteBatchIds(ids);
        return success();
    }

    /**
     * 获取收支汇总数据列表
     *
     * @param current       页码
     * @param size          条数
     * @param zhId          账户id
     * @param startDateLong 起始时间时间戳
     * @param endDateLong   结束时间时间戳
     */
    @GetMapping("/cert/iototal")
    public Result listIOTotal(
            @RequestParam(defaultValue = "1") Integer current, @RequestParam(defaultValue = "10") Integer size,
            
            @RequestParam(name = "zhId", required = false) Long zhId,
            @RequestParam(name = "startDate", required = false) Long startDateLong,
            @RequestParam(name = "endDate", required = false) Long endDateLong) {
        Result result = success();

        //TODO L 性能待优化

        Page<IOTotalDto> page = getTotalByPart(current, size, LoginUser.getCompanyId(), zhId);

        //可变类型封装的页码
        Holder<Integer> currentPro = new Holder<>();
        currentPro.set(current);

        Predicate<Page<IOTotalDto>> isDataLessFunc = p -> p.getRecords().size() < size;//分页数据少于请求条数

        boolean isOne = page.getCurrent() == INTEGER_ONE;//是否查询第一页
        //从DB查询的数据，需要后端处理才能得知是否有效。例如出现第一页数据均无效，但后续页的数据有效，就可以借此获取其他页数据塞到第一页。
        boolean plus2One = (page.getTotal() > page.getSize()) && isOne;
        do {
            boolean isPlus = isPlusRecord(plus2One, isDataLessFunc.test(page), currentPro, size, page.getTotal());
            if (isPlus) {
                current = currentPro.get();
                page.getRecords().addAll(getTotalByPart(currentPro.get(), size, LoginUser.getCompanyId(), zhId).getRecords());
            } else
                break;

            result = fixTotalList(page.getRecords());
            if (result.getCode() == 500)
                return result;

            //科目id与汇总表的映射
            Map<Long, IOTotalDto> subjMap = page.getRecords().stream().collect(Collectors.toMap(IOTotalDto::getSubjectsId, d -> d));
            subjMap.values().forEach(d -> initMoneyByNow(Collections.singletonList(d)));

            //期初余额计算
            List<Certificate> certs2Ori = cashierService.listCertificate(current, size, LoginUser.getCompanyId(), 0L, null, startDateLong, endDateLong, Boolean.FALSE, Boolean.TRUE).getRecords();
            initMoney(certs2Ori, subjMap::containsKey, sId -> Collections.singletonList(subjMap.get(sId)), false);

            //收入、支出计算
            List<Certificate> certs2IO = getCert2Cal(zhId, current, size, LoginUser.getCompanyId(), startDateLong, endDateLong, certs2Ori, page.getRecords());
            initMoney(certs2IO, subjMap::containsKey, sId -> Collections.singletonList(subjMap.get(sId)), true);

            //期末余额计算
            subjMap.values().forEach(d -> initEndMoney(Collections.singletonList(d)));

            //初始化币别名称
            result = initPriceTypeName(subjMap.values(), LoginUser.getCompanyId());
            if (result.getCode() == 500)
                return result;

            /**
             * TODO L BUG： 返回给前端的数据，从DB查出来后，需要在后端进行处理才能得知数据是否有效。
             *  -   但如果不把后续页的数据也进行处理，也不知道这些数据是否有效，但处理这些额外数据又太冗余。
             *  -   所以暂时仅第一页的数据删除无效数据，后续页不管。
             */
            if (isOne)
                removeInvalidByTotalOfOne(page.getRecords());

            //可能会出现使用后续页填充第一页，但处理后留下的数据大于条数，就截取满足
            if (plus2One && (page.getRecords().size() > size))
                page.setRecords(page.getRecords().subList(0, size));
        } while (plus2One && isDataLessFunc.test(page));

        //重置分页数据
        boolean isDoneDataLess = (page.getTotal() > size) && isDataLessFunc.test(page);//是否实际有效数据少于DB返回的total
        boolean isZhIdSelect = !ObjectUtils.isEmpty(zhId) && (zhId > LONG_ZERO);//是否指定账户id查询
        if (isDoneDataLess || isZhIdSelect)
            page.setTotal(page.getRecords().size());
        result.setData(page);

        return result;
    }

    /**
     * 获取收支汇总表的部分数据列表
     *
     * @param current   页码
     * @param size      条数
     * @param companyId 公司id
     * @param zhId      账户id
     */
    private Page<IOTotalDto> getTotalByPart(Integer current, Integer size, Long companyId, Long zhId) {
        return zhService.selectJoinListPage(new Page<>(current, size), IOTotalDto.class,
                new MPJLambdaWrapper<ZhangHu>()
                        .selectAs(ZhangHu::getCode, IOTotalDto::getZhCode)
                        .selectAs(ZhangHu::getName, IOTotalDto::getZhName)
                        .select(ZhangHu::getMTypeId, ZhangHu::getSubjectsId)
                        .eq(ZhangHu::getIsActive, 1)
                        .eq(ZhangHu::getCompanyId, companyId)
                        .eq(!ObjectUtils.isEmpty(zhId) && (zhId > NumberUtils.LONG_ZERO), ZhangHu::getId, zhId)
                        .orderByAsc(ZhangHu::getCreateTime)
        );
    }

    /**
     * 是否补充分页数据
     *
     * @param plus2One   是否第一页需要补充分页数据
     * @param isDataLess 是否分页数据少于请求条数
     * @param currentPro 页码
     * @param size       条数
     * @param total      DB返回的总条数
     */
    private boolean isPlusRecord(boolean plus2One, boolean isDataLess, Holder<Integer> currentPro, Integer size, long total) {
        if (plus2One && isDataLess) {
            if ((long) currentPro.get() * size >= total)
                return false;

            currentPro.set(currentPro.get() + 1);
        }
        return true;
    }

    /**
     * 获取正确的收支汇总列表
     * @param toFixList 待修复列表
     */
    private Result<Boolean> fixTotalList(List<IOTotalDto> toFixList) {
        //TODO L SQL待修正，即分组后只取首个

        //多账户可能会绑定同一账目，所以收支汇总表仅展示首位账户。
        Map<Long, List<IOTotalDto>> subjMap = toFixList.stream()
                .filter(t -> !ObjectUtils.isEmpty(t.getSubjectsId()))
                .collect(Collectors.groupingBy(IOTotalDto::getSubjectsId));
        toFixList.clear();

        if (ObjectUtils.isEmpty(subjMap))
            return Result.failed("账户需要设置入账科目");

        for (List<IOTotalDto> nowList : subjMap.values())
            toFixList.add(nowList.get(0));

        return Result.success();
    }

    /**
     * 获取计算收入、支出的凭证列表
     *
     * @param zhId         账户id
     * @param current      页码
     * @param size         条数
     * @param companyId    公司id
     * @param startDateLong 起始时间时间戳
     * @param endDateLong   结束时间时间戳
     * @param certs2Ori    期初余额计算使用的凭证列表
     * @param totalsByPage 分页中的数据列表
     * @return
     */
    private List<Certificate> getCert2Cal(long zhId, Integer current, Integer size, Long companyId, Long startDateLong, Long endDateLong, List<Certificate> certs2Ori, List<IOTotalDto> totalsByPage) {
        List<Certificate> result;
        if (zhId != INTEGER_ZERO) {
            result = cashierService.listCertificate(current, size, companyId, zhId, null, startDateLong, endDateLong, Boolean.TRUE, Boolean.TRUE).getRecords();
            if (result.isEmpty() && certs2Ori.isEmpty())
                totalsByPage.clear();
        } else
            result = getCertListByNow(companyId, startDateLong, endDateLong);

        sortByAbstList(result);
        return result;
    }

    /**
     * 初始化金额相关实例域
     *
     * @param certList  凭证列表
     * @param isProcess 科目id与汇总表的映射
     * @param isNow     true: 当月；false:本月之前
     */
    private void initMoney(List<Certificate> certList, Predicate<Long> isProcess, Function<Long, List<? extends BaseMoneyByCashierDto>> getDto, boolean isNow) {
        for (Certificate cert : certList) {
            List<CertificateAbstract> abstList = cert.getAbstracts();

            //[当前凭证]的[凭证摘要]列表的首个[凭证摘要]的[科目id]和账户的[科目id]匹配才可以初始化金额
            if (!ObjectUtils.isEmpty(abstList)) {
                Long subjId = abstList.get(0).getAccountId();
                if (isProcess.test(subjId)) {
                    getDto.apply(subjId).forEach(dto -> initMoneyByOne(abstList, dto, isNow));
                }
            }
        }
    }

    /**
     * 给当前收支汇总表的金额相关实例域赋初值
     *
     * @param dtoList 多态金额实例列表
     */
    private void initMoneyByNow(Collection<? extends BaseMoneyByCashierDto> dtoList) {
        dtoList.forEach(dto -> {
            dto.setOriMoney(new BigDecimal(0));
            dto.setBorrowMoney(new BigDecimal(0));
            dto.setLoansMoney(new BigDecimal(0));
        });
    }

    /**
     * 同一科目的收支汇总表数据赋值
     *
     * @param abstList 凭证摘要列表
     * @param dto      多态金额相关实例
     * @param isNow    true: 当月；false:本月之前
     */
    private void initMoneyByOne(List<CertificateAbstract> abstList, BaseMoneyByCashierDto dto, boolean isNow) {
        for (CertificateAbstract abst : abstList) {
            //收入、期初余额计算
            if (!ObjectUtils.isEmpty(abst.getBorrowMoney()) && abst.getBorrowMoney() > 0) {
                BigDecimal bMoneyByNow = new BigDecimal(abst.getBorrowMoney());

                if (isNow) {
                    dto.setBorrowMoney(dto.getBorrowMoney().add(bMoneyByNow));
                } else
                    dto.setOriMoney(dto.getOriMoney().add(bMoneyByNow));
            }

            //支出、期初余额计算
            if (!ObjectUtils.isEmpty(abst.getLoansMoney()) && abst.getLoansMoney() > 0) {
                BigDecimal lMoneyByNow = new BigDecimal(abst.getLoansMoney());

                if (isNow)
                    dto.setLoansMoney(dto.getLoansMoney().add(lMoneyByNow));
                else
                    dto.setOriMoney(dto.getOriMoney().subtract(lMoneyByNow));
            }
        }
    }

    /**
     * 获取当月凭证列表
     *
     * @param companyId 公司id
     * @param startDateLong 起始时间时间戳
     * @param endDateLong   结束时间时间戳
     */
    private List<Certificate> getCertListByNow(Long companyId, Long startDateLong, Long endDateLong) {
        //TODO L 待修正调用注释的方法
        //        return cashierService.listCertificate(INTEGER_ZERO, INTEGER_ZERO, companyId, LONG_ZERO, dateStr, Boolean.TRUE, Boolean.FALSE).getRecords();

        return certificateService.selectJoinList(Certificate.class, new MPJLambdaWrapper<Certificate>()
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
                .leftJoin(CertificateFile.class, CertificateFile::getCertificateId, Certificate::getId)

                .eq(Certificate::getCompanyId, companyId)
                .ge(Certificate::getDate, DateUtil.date(startDateLong))
                .lt(Certificate::getDate, DateUtil.date(endDateLong))
                .orderByDesc(Certificate::getDate)
        );
    }

    /**
     * 初始化期末余额
     *
     * @param dtoList 多态金额相关实例列表
     */
    private void initEndMoney(List<? extends BaseMoneyByCashierDto> dtoList) {
        dtoList.forEach(dto -> {
            BigDecimal oriMoney = dto.getOriMoney();
            BigDecimal bMoney = dto.getBorrowMoney();
            BigDecimal lMoney = dto.getLoansMoney();

            dto.setEndMoney(oriMoney.add(bMoney).subtract(lMoney));
        });
    }

    /**
     * 初始化币别名称
     *
     * @param dtos      收支汇总表列表
     * @param companyId 公司id
     */
    private Result initPriceTypeName(Collection<IOTotalDto> dtos, Long companyId) {
        List<Long> mTypeIds = dtos.stream().map(IOTotalDto::getMTypeId).collect(Collectors.toList());
        if (ObjectUtils.isEmpty(mTypeIds))
            return Result.failed("need create zhanghu");
        List<PriceType> mTypes = priceTypeService.selectJoinList(PriceType.class, new MPJLambdaWrapper<PriceType>()
                .select(PriceType::getId, PriceType::getName)
                .eq(PriceType::getCompanyId, companyId)
                .in(PriceType::getId, mTypeIds)
        );

        Map<Long, List<IOTotalDto>> mTypeMap = dtos.stream().collect(Collectors.groupingBy(IOTotalDto::getMTypeId));
        mTypes.forEach(t -> {
            List<IOTotalDto> dtoList = mTypeMap.get(t.getId());
            dtoList.forEach(d -> d.setMTypeName(t.getName()));
        });

        return Result.success();
    }

    /**
     * 移除第一页不符合要求的收支汇总数据
     *
     * @param totalList 收支汇总列表
     */
    private void removeInvalidByTotalOfOne(List<IOTotalDto> totalList) {
        totalList.removeIf(this::hasMoney);
    }

    /**
     * 出纳基础金额的值是否是有效值
     *
     * @param moneyImpl 出纳基础金额实现
     * @return
     */
    private boolean hasMoney(BaseMoneyByCashierDto moneyImpl) {
        BigDecimal zero = new BigDecimal(INTEGER_ZERO);
        boolean nonOri = moneyImpl.getOriMoney().compareTo(zero) == 0;
        boolean nonBorr = moneyImpl.getBorrowMoney().compareTo(zero) == 0;
        boolean nonLoan = moneyImpl.getLoansMoney().compareTo(zero) == 0;
        boolean nonEnd = moneyImpl.getEndMoney().compareTo(zero) == 0;

        if (nonOri && nonBorr && nonLoan && nonEnd)
            return true;
        return false;
    }

    /**
     * 导出收支汇总表
     *
     * @param current   页码
     * @param size      条数
     * @param zhangHuId 账户id
     * @param startDateLong 起始时间时间戳
     * @param endDateLong   结束时间时间戳
     */
    @GetMapping("/exportTotal")
    public void exportTotal(HttpServletRequest req, HttpServletResponse resp,
                            @RequestParam(defaultValue = "1") Integer current, @RequestParam(defaultValue = "10") Integer size,
                             @RequestParam Long zhangHuId,
                            @RequestParam(name = "startDate", required = false) Long startDateLong,
                            @RequestParam(name = "endDate", required = false) Long endDateLong) {
        Result<Page<IOTotalDto>> oriResult = listIOTotal(current, size, zhangHuId, startDateLong, endDateLong);
        if (!oriResult.getCode().equals(Result.success().getCode()))
            return;

        Consumer<ExcelWriter> initSubTitleFunc = w -> initSubTitleByTotal(w,
                LoginUser.get().getName(),
                getMonthRange(startDateLong, endDateLong));

        List<IOTotalDto> oriData = oriResult.getData().getRecords();
        exportCore(req, resp,
                oriData, Function.identity(), 6, "ShouZhiHuiZong", "收支汇总表", this::excelMapByTotal, initSubTitleFunc);
    }

    /**
     * 表格-实体类映射[收支汇总表]
     *
     * @param writer
     */
    private void excelMapByTotal(ExcelWriter writer) {
        writer.addHeaderAlias("zhCode", "账户编码");
        writer.addHeaderAlias("zhName", "账户名称");
        writer.addHeaderAlias("mTypeName", "币别名称");
        writer.addHeaderAlias("oriMoney", "期初余额");
        writer.addHeaderAlias("borrowMoney", "收入");
        writer.addHeaderAlias("loansMoney", "支出");
        writer.addHeaderAlias("endMoney", "期末余额");
    }

    /**
     * 初始化[收支汇总表]表格的子标题
     *
     * @param userName     用户名称
     * @param dateRangeStr 当月起始、结束日区间字符串
     */
    private void initSubTitleByTotal(ExcelWriter writer, String userName, String dateRangeStr) {
        writer.merge(1, 1, 0, 2, userName, false);
        writer.merge(1, 1, 3, 6, dateRangeStr, false);
    }

    /**
     * 获取核对总账列表
     *
     * @param current   页码
     * @param size      条数
     * @param startDateLong 起始时间时间戳
     * @param endDateLong   结束时间时间戳
     */
    @GetMapping("/cert/confirm")
    public Result<Page<ConfirmTotalDto>> listConfirm(
            @RequestParam(defaultValue = "1") Integer current, @RequestParam(defaultValue = "10") Integer size,
            
            @RequestParam(name = "startDate", required = false) Long startDateLong,
            @RequestParam(name = "endDate", required = false) Long endDateLong) {
        Result result = Result.success();

        //TODO L 性能待优化

        Page<ConfirmTotalDto> page = initConfrimPage(current, size, LoginUser.getCompanyId());

        //可变类型封装的页码
        Holder<Integer> currentPro = new Holder<>();
        currentPro.set(current);

        Predicate<Page<ConfirmTotalDto>> isDataLessFunc = p -> p.getRecords().size() < size;//分页数据少于请求条数

        boolean isOne = page.getCurrent() == INTEGER_ONE;//是否查询第一页
        //从DB查询的数据，需要后端处理才能得知是否有效。例如出现第一页数据均无效，但后续页的数据有效，就可以借此获取其他页数据塞到第一页。
        boolean plus2One = (page.getTotal() > page.getSize()) && isOne;
//        do {
//            boolean isPlus = isPlusRecord(plus2One, isDataLessFunc.test(page), currentPro, size, page.getTotal());
//            if (isPlus)
//                current = currentPro.get();
//            else
//                break;

        //科目id和核对总账的映射
        Map<Long, ConfirmTotalDto> subjMap = page.getRecords().stream()
                .filter(c -> !ObjectUtils.isEmpty(c.getSubj()))
                .collect(Collectors.toMap(c -> c.getSubj().getSubjectsId(), d -> d));
//            if (ObjectUtils.isEmpty(subjMap))
//                continue;

        //科目id列表
        List<Long> subjIds = page.getRecords().stream().map(ConfirmTotalDto::getSubj).map(ConfirmTotalDto.SubjDto::getSubjectsId).collect(Collectors.toList());

        //不同时间区间的凭证列表
        List<Certificate> certsByBefore = cashierService.listCertificate(current, size, LoginUser.getCompanyId(), 0L, null, startDateLong, endDateLong, Boolean.FALSE, Boolean.TRUE).getRecords();
        List<Certificate> certsByBeNow = getCertListByNow(LoginUser.getCompanyId(), startDateLong, endDateLong);

        //初始化科目实例
        initTotalByConfirm(subjMap, certsByBefore, certsByBeNow, c -> Collections.singletonList(c.getSubj()));
        initSubjName(subjIds, subjMap);

        //初始化子科目列表
        initDownSubj(subjIds, page.getRecords(), subjMap, certsByBefore, certsByBeNow);

        //初始化差额实例
        initEndMoney(subjMap.values());

        /**
         * TODO L BUG： 返回给前端的数据，从DB查出来后，需要在后端进行处理才能得知数据是否有效。
         *  -   但如果不把后续页的数据也进行处理，也不知道这些数据是否有效，但处理这些额外数据又太冗余。
         *  -   所以暂时仅第一页的数据删除无效数据，后续页不管。
         */
//            if (isOne)
//                removeInvalidByConfirmOfOne(page.getRecords());

        //可能会出现使用后续页填充第一页，但处理后留下的数据大于条数，就截取满足
//            if (plus2One && (page.getRecords().size() > size))
//                page.setRecords(page.getRecords().subList(0, size));
//        } while (plus2One && isDataLessFunc.test(page));

        //重置分页数据
//        boolean isDoneDataLess = (page.getTotal() > size) && isDataLessFunc.test(page);//是否实际有效数据少于DB返回的total
//        if (isDoneDataLess)
//            page.setTotal(page.getRecords().size());
        result.setData(page);

        return result;
    }

    /**
     * 初始化核对总账分页
     *
     * @param current   页码
     * @param size      条数
     * @param companyId 公司id
     * @return
     */
    private Page<ConfirmTotalDto> initConfrimPage(Integer current, Integer size, Long companyId) {
        //分页核心数据赋值
        Page<ConfirmTotalDto.SubjDto> tmpPage = getSubjIds(current, size, companyId);
        List<ConfirmTotalDto> records = new ArrayList<>();
        tmpPage.getRecords().forEach(s -> {
            ConfirmTotalDto now = new ConfirmTotalDto();
            now.setSubj(s);
            records.add(now);
        });

        //分页杂项数据赋值
        Page<ConfirmTotalDto> page = new Page<>();
        page.setCurrent(tmpPage.getCurrent());
        page.setSize(tmpPage.getSize());
        page.setTotal(tmpPage.getTotal());
        page.setRecords(records);

        return page;
    }

    /**
     * 获取科目id列表
     *
     * @param current   页码
     * @param size      条数
     * @param companyId 公司id
     * @return
     */
    private Page<ConfirmTotalDto.SubjDto> getSubjIds(Integer current, Integer size, Long companyId) {
        //TODO L 应该改完日记账，才能顺应改它。
        return zhService.selectJoinListPage(new Page<>(current, size), ConfirmTotalDto.SubjDto.class,
                new MPJLambdaWrapper<ZhangHu>()
                        .select(ZhangHu::getSubjectsId)
                        .eq(ZhangHu::getIsActive, 1)
                        .eq(ZhangHu::getCompanyId, companyId)
                        .groupBy(ZhangHu::getSubjectsId)
                //TODO L 分组支持null
        );
    }

    /**
     * 初始化核对总账的科目实例
     *
     * @param subjMap       科目id和核对总账的映射
     * @param certsByBefore 本月前的凭证列表
     * @param certsByBeNow  本月的凭证列表
     * @param getMoneyImpl  获取多态金额实例
     */
    private <T> void initTotalByConfirm
    (Map<Long, T> subjMap, List<Certificate> certsByBefore, List<Certificate> certsByBeNow, Function<T, List<? extends
            BaseMoneyByCashierDto>> getMoneyImpl) {
        //金额相关实例域赋初值
        subjMap.values().forEach(d -> initMoneyByNow(getMoneyImpl.apply(d)));

        //期初余额计算
        initMoney(certsByBefore, sId -> subjMap.containsKey(sId), sId -> getMoneyImpl.apply(subjMap.get(sId)), false);

        //收入、支出计算
        initMoney(certsByBeNow, sId -> subjMap.containsKey(sId), sId -> getMoneyImpl.apply(subjMap.get(sId)), true);

        //期末余额计算
        subjMap.values().forEach(d -> initEndMoney(getMoneyImpl.apply(d)));
    }

    /**
     * 初始化科目总账名称
     *
     * @param subjIds 科目id列表
     * @param subjMap 科目id和核对总账的映射
     */
    private void initSubjName(List<Long> subjIds, Map<Long, ConfirmTotalDto> subjMap) {
        List<SubjectsNameDto> subjs = accountService.selectJoinList(SubjectsNameDto.class,
                new MPJLambdaWrapper<Account>()
                        .select(Account::getId, Account::getNo, Account::getName)
                        .in(Account::getId, subjIds)
        );
        subjs.forEach(s -> {
            ConfirmTotalDto.SubjDto subj = subjMap.get(s.getId()).getSubj();
            subj.setNo(s.getNo());
            subj.setProjName(s.getName());
        });
    }

    /**
     * 初始化子科目列表
     *
     * @param subjIds       科目id列表
     * @param upSubjList    [父]科目列表
     * @param subjMap       科目id和核对总账的映射
     * @param certsByBefore 当月前的凭证列表
     * @param certsByBeNow  当月凭证列表
     */
    private void initDownSubj(List<Long> subjIds, List<ConfirmTotalDto> upSubjList, Map<Long, ConfirmTotalDto> subjMap, List<Certificate> certsByBefore, List<Certificate> certsByBeNow) {
        initDownSubjByBase(subjIds, upSubjList);
        for (ConfirmTotalDto dto : subjMap.values()) {
            List<ConfirmTotalDto.BaseTotalDto> zhTotals = dto.getDownSubj();
            if (ObjectUtils.isEmpty(zhTotals))//子科目为空说明，该科目没有子科目
                continue;

            //科目总账id[父]和子科目总账的映射
            Map<Long, List<ConfirmTotalDto.BaseTotalDto>> subjForZh = zhTotals.stream().collect(Collectors.groupingBy(ConfirmTotalDto.BaseTotalDto::getSubjectsId));

            initTotalByConfirm(subjForZh, certsByBefore, certsByBeNow, z -> z);
        }
    }

    /**
     * 初始化子科目列表[基础]
     *
     * @param subjIds 科目id列表
     * @param dtos    核对总账列表
     */
    private void initDownSubjByBase(List<Long> subjIds, List<ConfirmTotalDto> dtos) {
        Map<Long, List<Account>> zhMap = getDownSubj(subjIds);

        for (ConfirmTotalDto dto : dtos) {
            //列表无该科目id，说明其没有子科目
            if (!subjIds.contains(dto.getSubj().getSubjectsId()))
                break;

            List<ConfirmTotalDto.BaseTotalDto> zhTotals = new ArrayList<>();

            //初始化子科目
            Long subjId = dto.getSubj().getSubjectsId();//科目id
            List<Account> zhList = zhMap.get(subjId);
            zhList.forEach(z -> {
                ConfirmTotalDto.BaseTotalDto zhTotal = new ConfirmTotalDto.BaseTotalDto();
                zhTotal.setProjName(z.getAccountName());
                zhTotal.setSubjectsId(z.getId());
                zhTotals.add(zhTotal);
            });

            dto.setDownSubj(zhTotals);
        }
    }

    /**
     * 获取子科目分组[key: 父科目id]
     *
     * @param subjIds 科目id列表
     * @return
     */
    private Map<Long, List<Account>> getDownSubj(List<Long> subjIds) {
        List<Account> tmpZhs = accountService.selectJoinList(Account.class,
                new MPJLambdaWrapper<Account>()
                        .select(Account::getId, Account::getAccountName, Account::getParentId)
                        .in(Account::getParentId, subjIds)
                        .orderByAsc(Account::getWeight)
        );
        Map<Long, List<Account>> result = tmpZhs.stream().collect(Collectors.groupingBy(Account::getParentId));

        //移除没有子科目的科目
        subjIds.removeIf(s -> !result.containsKey(s));

        return result;
    }

    /**
     * 初始化差额
     *
     * @param confirmList 核对总账列表
     */
    private void initEndMoney(Collection<ConfirmTotalDto> confirmList) {
        confirmList.forEach(c -> {
            //差额
            ConfirmTotalDto.BaseTotalDto lessTotal = getEndMoney(c.getSubj());

            //金额计算
            if (!ObjectUtils.isEmpty(c.getDownSubj()))
                c.getDownSubj().forEach(d -> endMoneyCal(d, lessTotal));

            c.setLessTotal(lessTotal);
        });
    }

    /**
     * 获取差额
     *
     * @param upSubj [父]科目
     * @return
     */
    private ConfirmTotalDto.BaseTotalDto getEndMoney(ConfirmTotalDto.SubjDto upSubj) {
        ConfirmTotalDto.BaseTotalDto lessTotal = new ConfirmTotalDto.BaseTotalDto();
        lessTotal.setOriMoney(upSubj.getOriMoney());
        lessTotal.setBorrowMoney(upSubj.getBorrowMoney());
        lessTotal.setLoansMoney(upSubj.getLoansMoney());
        lessTotal.setEndMoney(upSubj.getEndMoney());
        lessTotal.setProjName("差额");
        return lessTotal;
    }

    /**
     * 差额计算并赋值
     *
     * @param subj      [父]科目总账
     * @param lessTotal 差额总账
     */
    private void endMoneyCal(ConfirmTotalDto.BaseTotalDto subj, ConfirmTotalDto.BaseTotalDto lessTotal) {
        //差额---期初余额
        endMoneyFunc(subj.getOriMoney(), lessTotal.getOriMoney(), m -> lessTotal.setOriMoney(m));
        //差额---收入余额
        endMoneyFunc(subj.getBorrowMoney(), lessTotal.getBorrowMoney(), m -> lessTotal.setBorrowMoney(m));
        //差额---支出余额
        endMoneyFunc(subj.getLoansMoney(), lessTotal.getLoansMoney(), m -> lessTotal.setLoansMoney(m));
        //差额---期末余额
        endMoneyFunc(subj.getEndMoney(), lessTotal.getEndMoney(), m -> lessTotal.setEndMoney(m));
    }

    /**
     * 差额计算并赋值[具体实现]
     *
     * @param subjMoney    [父]科目相应金额
     * @param lessMoney    差额总账相应金额
     * @param lessMoneySet 差额总账金额赋值
     */
    private void endMoneyFunc(BigDecimal subjMoney, BigDecimal lessMoney, Consumer<BigDecimal> lessMoneySet) {
        switch (subjMoney.compareTo(BigDecimal.ZERO)) {
            case 1:
                lessMoneySet.accept(lessMoney.subtract(subjMoney));
                break;
            case -1:
                lessMoneySet.accept(lessMoney.add(subjMoney));
                break;
        }
    }

    /**
     * 移除第一页不符合要求的核对总账
     *
     * @param totalList 核对总账列表
     */
    private void removeInvalidByConfirmOfOne(List<ConfirmTotalDto> totalList) {
        List<Boolean> tmpList = new ArrayList<>();
        totalList.removeIf(t -> {
            tmpList.clear();

            tmpList.add(hasMoney(t.getSubj()));
            if (!ObjectUtils.isEmpty(t.getDownSubj()))
                t.getDownSubj().forEach(d -> tmpList.add(hasMoney(d)));
            tmpList.add(hasMoney(t.getLessTotal()));

            tmpList.removeIf(Boolean::booleanValue);
            if (tmpList.size() == INTEGER_ZERO)
                return true;
            else
                return false;
        });
    }

    /**
     * 导出核对总账
     *
     * @param current   页码
     * @param size      条数
     * @param startDateLong 起始时间时间戳
     * @param endDateLong   结束时间时间戳
     */
    @GetMapping("/exportConfirm")
    public void exportConfirm(HttpServletRequest req, HttpServletResponse resp,
                              @RequestParam(defaultValue = "1") Integer current, @RequestParam(defaultValue = "10") Integer size,
                              
                              @RequestParam(name = "startDate", required = false) Long startDateLong,
                              @RequestParam(name = "endDate", required = false) Long endDateLong) {
        List<ConfirmTotalDto> oriData = listConfirm(current, size, startDateLong, endDateLong).getData().getRecords();

        Consumer<ExcelWriter> initSubTitleFunc = w -> initSubTitleByConfirm(w,
                LoginUser.get().getName(),
                getMonthRange(startDateLong, endDateLong));

        exportCore(req, resp,
                oriData, this::getExcelDatasByConfirm, 4, "HeDuiZongZhang", "核对总账", this::excelMapByConfirm, initSubTitleFunc);
    }

    /**
     * 获取[核对总账]的表格数据
     *
     * @param oriData 原始数据
     * @return
     */
    private List<ConfirmTotalDto.BaseTotalDto> getExcelDatasByConfirm(List<ConfirmTotalDto> oriData) {
        List<ConfirmTotalDto.BaseTotalDto> result = new ArrayList<>();

        oriData.forEach(c -> {
            result.add(c.getSubj());

            if (!ObjectUtils.isEmpty(c.getDownSubj()))
                c.getDownSubj().forEach(result::add);

            result.add(c.getLessTotal());
        });

        return result;
    }

    /**
     * 表格-实体类映射[核对总账]
     */
    private void excelMapByConfirm(ExcelWriter writer) {
        writer.addHeaderAlias("projName", "项目");
        writer.addHeaderAlias("oriMoney", "期初余额");
        writer.addHeaderAlias("borrowMoney", "收入");
        writer.addHeaderAlias("loansMoney", "支出");
        writer.addHeaderAlias("endMoney", "期末余额");
    }

    /**
     * 初始化[核对总账]表格的子标题
     *
     * @param userName     用户名称
     * @param dateRangeStr 当月起始、结束日区间字符串
     */
    private void initSubTitleByConfirm(ExcelWriter writer, String userName, String dateRangeStr) {
        writer.merge(1, 1, 0, 1, userName, false);
        writer.merge(1, 1, 2, 4, dateRangeStr, false);
    }
}