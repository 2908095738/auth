package com.bbs.financial.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.financial.entity.*;
import com.bbs.financial.service.NoteService;
import com.bbs.financial.mapper.NoteMapper;
import com.bbs.financial.service.ZhangHuService;
import com.bbs.financial.util.LoginUser;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;

import java.util.Collection;
import java.util.Date;
import java.util.Objects;

import static java.util.Objects.nonNull;

/**
* @author Mafty
* @description 针对表【note(日记账)】的数据库操作Service实现
* @createDate 2024-06-29 15:36:24
*/
@Service
public class NoteServiceImpl extends MPJBaseServiceImpl<NoteMapper, Note>
    implements NoteService{

    @Resource
    private ZhangHuService zhService;

    @Override
    public Page<Note> listNote(Integer current, Integer size, Collection<Long> zhIdList, Boolean isAllZh, Integer voucherStatus, Integer noteType, String certificateAbstract, String remark, Long dateLong, Long startDateLong, Long endDateLong, boolean isMonth, boolean isPage) {
        //TODO L SQL合一

        //结束时间时间戳转成当日最后一秒的时间戳
        if (!ObjectUtils.isEmpty(endDateLong) && endDateLong > 0) {
            Long hourOf23 = 23 * 60 * 60 * 1000L;
            Long minuteOf59 = 59 * 60 * 1000L;
            Long secondOf59 = 59 * 1000L;
            endDateLong = endDateLong + hourOf23 + minuteOf59 + secondOf59;
        }

        MPJLambdaWrapper<Note> wrappers = getDataWrapperByNoteList();

        boolean nonZhId = !isZhId(zhIdList);
        if (isMonth && nonZhId && isPage) {
            return selectJoinListPage(new Page<>(current, size), Note.class, getConditionByNoteList(wrappers, !nonZhId, zhIdList, isAllZh, voucherStatus, noteType, certificateAbstract, remark, isMonth, dateLong, startDateLong, endDateLong));
        } else {
            return new Page<Note>().setRecords(selectJoinList(Note.class,
                    getConditionByNoteList(wrappers, !nonZhId, zhIdList, isAllZh, voucherStatus, noteType, certificateAbstract, remark, isMonth, dateLong, startDateLong, endDateLong)));
        }
    }

    /**
     * 是否有账户id列表
     *
     * @param zhIdList 账户id列表
     */
    private boolean isZhId(Collection<Long> zhIdList) {
        boolean nonZhId = ObjectUtils.isEmpty(zhIdList);
        boolean isLess = zhIdList.size() < NumberUtils.INTEGER_TWO;
        boolean isZero = zhIdList.stream().filter(z -> z.equals(NumberUtils.LONG_ZERO)).count() < NumberUtils.LONG_ONE;

        return !nonZhId && isLess && isZero;
    }

    /**
     * 获取凭证列表输出实例域的Wrapper
     */
    private MPJLambdaWrapper<Note> getDataWrapperByNoteList() {
        return new MPJLambdaWrapper<Note>()
                .selectAll(Note.class)
                .selectAssociation(Account.class, Note::getHeAccount)
                .selectAssociation(ZhangHu.class, Note::getZhangHu)
                .selectAssociation(Certificate.class, Note::getCertificate)

                .leftJoin(Account.class, Account::getId, Note::getHeAccountId)
                .leftJoin(ZhangHu.class, ZhangHu::getId, Note::getZhId)
                .leftJoin(Certificate.class, Certificate::getId, Note::getCertificateId);
    }

    /**
     * 获取查询凭证列表条件
     *
     * @param isZhId              有账户id
     * @param zhIdList            账户id列表
     * @param isAllZh             是否显示所有账户：true: 显示所有;false: 显示启用;
     * @param voucherStatus       凭证状态：0.所有凭证;1.未生成凭证;2.已生成凭证;
     * @param noteType            日记账类型：1.普通类型;0.初始金额
     * @param certificateAbstract 摘要
     * @param remark              备注
     * @param isMonth             true：当月；false：当月及之前
     * @param dateLong            时间戳
     * @param startDateLong       起始时间时间戳
     * @param endDateLong         结束时间时间戳
     */
    private MPJLambdaWrapper<Note> getConditionByNoteList(MPJLambdaWrapper<Note> wrappers, boolean isZhId, Collection<Long> zhIdList, Boolean isAllZh, Integer voucherStatus, Integer noteType, String certificateAbstract, String remark, boolean isMonth, Long dateLong, Long startDateLong, Long endDateLong) {
        return wrappers
                .eq(Note::getAccountingSetId, LoginUser.getLoginSetId())
                .eq(Objects.nonNull(isAllZh)&&!isAllZh, ZhangHu::getIsActive, NumberUtils.INTEGER_ONE)
                .in(isZhId, Note::getZhId, zhIdList)

                //凭证状态查询条件
                .isNull(nonNull(voucherStatus) && voucherStatus.equals(NumberUtils.INTEGER_ONE), Note::getCertificateId)
                .isNotNull(nonNull(voucherStatus) && voucherStatus.equals(NumberUtils.INTEGER_TWO), Note::getCertificateId)

                //日记账类型查询条件
                .eq(!ObjectUtils.isEmpty(noteType), Note::getNoteType, noteType)

                //模糊查询条件
                .like(StringUtils.isNotBlank(certificateAbstract), Note::getCertificateAbstract, certificateAbstract)
                .like(StringUtils.isNotBlank(remark), Note::getRemark, remark)

                //日期查询条件
                .and(nonNull(startDateLong) && nonNull(endDateLong) && !startDateLong.equals(endDateLong), ext -> ext
                        .ge(Note::getDate, new Date(startDateLong))
                        .lt(Note::getDate, new Date(endDateLong))
                )

                //出纳期间查询条件
                .and(nonNull(startDateLong) && nonNull(endDateLong) && startDateLong.equals(endDateLong), ext -> ext
                        .ge(Note::getDate, DateUtil.beginOfMonth(new Date(startDateLong)))
                        .lt(Note::getDate, DateUtil.endOfMonth(new Date(endDateLong)))
                )

                .lt(!isMonth, Note::getDate, DateUtil.beginOfMonth(nonNull(dateLong) ? new Date(dateLong) : new Date()))

                .orderByAsc(Note::getDate);
    }
}




