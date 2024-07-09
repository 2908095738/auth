package com.bbs.financial.api.note.search;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.api.auth.User;
import com.bbs.api.auth.UserAPI;
import com.bbs.financial.dto.NoteDto;
import com.bbs.financial.entity.*;
import com.bbs.financial.service.NoteService;
import com.bbs.financial.service.PriceTypeService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;

@RestController
@RequestMapping
public class SearchNote {
    @Resource
    private NoteService orm;

    @DubboReference
    private UserAPI userAPI;

    @Resource
    private PriceTypeService priceTypeService;

    /**
     * 获取日记账
     *
     * @param id 日记账id
     */
    @GetMapping("/note/{id}")
    public Result<NoteDto> search(@PathVariable Long id) {
        Note tmpNote = orm.selectJoinOne(Note.class, new MPJLambdaWrapper<Note>().selectAssociation(Account.class, Note::getHeAccount).selectAssociation(ZhangHu.class, Note::getZhangHu)

                .leftJoin(Account.class, Account::getId, Note::getHeAccountId).leftJoin(ZhangHu.class, ZhangHu::getId, Note::getZhId)

                .eq(Note::getId, id));

        //初始化日记账的用户相关
        List<Note> tmpList = new ArrayList();
        tmpList.add(tmpNote);
        fillUser(tmpList, searchIdUserMap(filterUserIds(tmpList)));

        //初始化实例域
        List<NoteDto> tmpDtoList = new ArrayList<>();
        NoteDto dto = getNoteDto(tmpNote);
        tmpDtoList.add(dto);
        BigDecimal oriMoney = getOriMoney(tmpNote.getZhId(), tmpNote.getDate().getTime(), INTEGER_ZERO, null, null);
        initMoney(tmpList, tmpDtoList, oriMoney);

        return Result.success(dto);
    }

    /**
     * 获取日记账列表
     *
     * @param current             页码
     * @param size                条数
     * @param zhangHuId           账户id
     * @param isAllZh             是否显示所有账户：true: 显示所有;false: 显示启用;
     * @param voucherStatus       凭证状态：0.所有凭证;1.未生成凭证;2.已生成凭证;
     * @param startDateLong       起始时间戳
     * @param endDateLong         结束时间戳
     * @param certificateAbstract 摘要
     * @param heSubjName          对方科目名称
     * @param remark              备注
     * @param makeName            制单人
     */
    @GetMapping("/note/list")
    public Result<Page<NoteDto>> search(@RequestParam(defaultValue = "1") Integer current, @RequestParam(defaultValue = "10") Integer size, @RequestParam Long zhangHuId, @RequestParam boolean isAllZh, @RequestParam(name = "voucherStatus", required = false) Integer voucherStatus, @RequestParam(name = "startDate", required = false) Long startDateLong, @RequestParam(name = "endDate", required = false) Long endDateLong, @RequestParam(name = "certificateAbstract", required = false) String certificateAbstract, @RequestParam(name = "heSubjName", required = false) String heSubjName, @RequestParam(name = "remark", required = false) String remark, @RequestParam(name = "makeName", required = false) String makeName) {
        Page<Note> tmpPage = orm.listNote(current, size, Collections.singletonList(zhangHuId), voucherStatus, certificateAbstract, remark, null, startDateLong, endDateLong, true, true);

        //账户启用状态过滤
        tmpPage.getRecords().removeIf(n -> n.getZhangHu().getIsActive().equals(isAllZh));
        if (tmpPage.getRecords().isEmpty())
            return Result.success(new Page<NoteDto>().setCurrent(current).setSize(size).setTotal(INTEGER_ZERO).setRecords(Collections.emptyList()));

        //初始化日记账的用户相关
        fillUser(tmpPage.getRecords(), searchIdUserMap(filterUserIds(tmpPage.getRecords())));

        //制单人过滤
        if (StringUtils.isNotBlank(makeName)) {
            tmpPage.getRecords().removeIf(n -> !n.getCreateUser().getName().contains(makeName));
        }

        //实体类转DTO
        List<NoteDto> resultList = new ArrayList<>();
        tmpPage.getRecords().forEach(n -> resultList.add(getNoteDto(n)));

        //对方科目名称过滤
        if (StringUtils.isNotBlank(heSubjName)) {
            resultList.removeIf(d -> !d.getHeSubjName().contains(heSubjName));
        }

        //初始化DTO中的币别名称、余额
        BigDecimal oriMoney = getOriMoney(zhangHuId, startDateLong, voucherStatus, certificateAbstract, remark);
        initMoney(tmpPage.getRecords(), resultList, oriMoney);

        //初始化处理后的分页
        Page<NoteDto> resultPage = new Page();
        resultPage.setCurrent(current).setSize(size).setTotal(tmpPage.getTotal()).setRecords(resultList);

        return Result.success(resultPage);
    }

    private Set<Long> filterUserIds(List<Note> noteList) {
        Set<Long> userIds = new HashSet<>();
        noteList.forEach(note -> userIds.add(note.getCreateBy()));
        return userIds;
    }

    private Map<Long, User> searchIdUserMap(Set<Long> userIds) {
        return userAPI.getUserList(userIds).stream().collect(Collectors.toMap(User::getId, user -> user));
    }

    private void fillUser(List<Note> noteList, Map<Long, User> map) {
        noteList.forEach(note -> note.setCreateUser(map.get(note.getCreateBy())));
    }

    private NoteDto getNoteDto(Note note) {
        NoteDto dto = new NoteDto();

        dto.setId(note.getId());
        dto.setDate(note.getDate());
        dto.setCertificateAbstract(note.getCertificateAbstract());
        dto.setZhName(note.getZhangHu().getName());
        dto.setHeSubjName(getSubjName(note.getHeAccount()));
        dto.setBorrowMoney(note.getBorrowMoney());
        dto.setLoansMoney(note.getLoansMoney());

        if (!ObjectUtils.isEmpty(note.getCertificate()))
            dto.setCertName(note.getCertificate().getCertificateWord().getMsg() + "-" + note.getCertificate().getNo());

        dto.setRemark(note.getRemark());
        dto.setMakeName(note.getCreateUser().getName());

        dto.setMoneyId(note.getZhangHu().getMTypeId());
        dto.setCertificateId(note.getCertificateId());
        dto.setZhSubjId(note.getZhangHu().getSubjectsId());
        dto.setHeSubjId(note.getHeAccountId());

        return dto;
    }

    /**
     * 获取对方科目名称
     *
     * @param subj 科目实例
     * @return
     */
    private String getSubjName(Account subj) {
        if (ObjectUtils.isEmpty(subj)) return "";

        if (!ObjectUtils.isEmpty(subj.getName())) return subj.getNo() + " " + subj.getName();
        else return subj.getNo() + " " + subj.getAccountName();
    }

    /**
     * 获取期初余额
     *
     * @param zhangHuId           账户id
     * @param date                日期时间戳字符串
     * @param voucherStatus       凭证状态：0.所有凭证;1.未生成凭证;2.已生成凭证;
     * @param certificateAbstract 摘要
     * @param remark              备注
     */
    private BigDecimal getOriMoney(Long zhangHuId, Long date, Integer voucherStatus, String certificateAbstract, String remark) {
        //计算期初余额
        List<Note> tmpList = orm.listNote(INTEGER_ZERO, INTEGER_ZERO, Collections.singletonList(zhangHuId), voucherStatus, certificateAbstract, remark, date, null, null, Boolean.FALSE, Boolean.FALSE).getRecords();
        BigDecimal oriMoeny = new BigDecimal(NumberUtils.LONG_ZERO);
        for (Note note : tmpList) {
            if (!ObjectUtils.isEmpty(note.getBorrowMoney())) oriMoeny = oriMoeny.add(note.getBorrowMoney());
            if (!ObjectUtils.isEmpty(note.getLoansMoney())) oriMoeny = oriMoeny.subtract(note.getLoansMoney());
        }

        return oriMoeny;
    }

    /**
     * 初始化币别名称、期初余额、期末余额
     *
     * @param noteList 日记账列表
     * @param dtoList  日记账DTO列表
     * @param oriMoney 期初余额
     */
    private void initMoney(List<Note> noteList, List<NoteDto> dtoList, BigDecimal oriMoney) {
        Set<Long> priceTypeIdColl = noteList.stream().map(Note::getZhangHu).map(ZhangHu::getMTypeId).collect(Collectors.toSet());
        noteList.clear();

        //币别id和币别名称的映射
        Map<Long, String> nameByIdOfMoney = null;
        if (!priceTypeIdColl.isEmpty())
            nameByIdOfMoney = priceTypeService.listByIds(priceTypeIdColl).stream().collect(Collectors.toMap(PriceType::getId, PriceType::getName));


        for (int i = 0; i < dtoList.size(); i++) {
            NoteDto dto = dtoList.get(i);

            dto.setStartMoney(oriMoney);

            if (!priceTypeIdColl.isEmpty()) dto.setMoneyName(nameByIdOfMoney.get(dto.getMoneyId()));

            //计算余额
            if (!ObjectUtils.isEmpty(dto.getBorrowMoney()) && dto.getBorrowMoney().doubleValue() != NumberUtils.DOUBLE_ZERO)
                dto.setSurplusMoney(oriMoney.add(dto.getBorrowMoney()));
            else if (!ObjectUtils.isEmpty(dto.getLoansMoney()) && dto.getLoansMoney().doubleValue() != NumberUtils.DOUBLE_ZERO)
                dto.setSurplusMoney(oriMoney.subtract(dto.getLoansMoney()));
            else {
                dto.setSurplusMoney(oriMoney);
            }

            oriMoney = dto.getSurplusMoney();
        }
    }

    /**
     * 该账户是否有日记账
     *
     * @param zhId 账户id
     */
    @GetMapping("/hasNote/{zhId}")
    public Result<Boolean> hasNote(@PathVariable Long zhId) {
        List<Note> tmp = orm.lambdaQuery().eq(Note::getZhId, zhId).list();
        return !tmp.isEmpty() ? Result.success(true) : Result.success(false);
    }
}