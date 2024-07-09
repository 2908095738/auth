package com.bbs.financial.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.financial.entity.Note;

import com.github.yulichang.base.MPJBaseService;

import java.util.Collection;

/**
* @author Mafty
* @description 针对表【note(日记账)】的数据库操作Service
* @createDate 2024-06-29 15:36:24
*/
public interface NoteService extends MPJBaseService<Note> {
    /**
     * 查询日记账
     *
     * @param current             页码
     * @param size                条数
     * @param zhIdList            账户id列表
     * @param voucherStatus       凭证状态：0.所有凭证;1.未生成凭证;2.已生成凭证;
     * @param certificateAbstract 摘要
     * @param remark              备注
     * @param dateLong            时间戳
     * @param startDateLong       起始时间时间戳
     * @param endDateLong         结束时间时间戳
     * @param isMonth             true：当月；false：当月及之前
     * @param isPage              true：分页；false：不分页
     */
    Page<Note> listNote(Integer current, Integer size,
                        Collection<Long> zhIdList, Integer voucherStatus,
                        String certificateAbstract, String remark,
                        Long dateLong, Long startDateLong, Long endDateLong,
                        boolean isMonth, boolean isPage);
}
