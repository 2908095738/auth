package com.bbs.financial.api.note.search;

import com.bbs.Result;
import com.bbs.financial.entity.Note;
import com.bbs.financial.service.NoteService;
import com.bbs.financial.util.LoginUser;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;

/**
 * 查询账户初始金额日记账
 */
@RestController
@RequestMapping
public class SearchOriByZh {

    @Resource
    private NoteService orm;

    /**
     * 查询账户初始金额日记账
     *
     * @param zhId 账户id
     */
    @GetMapping("/note/zh/ori/{zhId}")
    public Result<Note> search(@PathVariable Long zhId) {
        return Result.success(orm.selectJoinOne(Note.class, new MPJLambdaWrapper<Note>()
                .eq(Note::getNoteType, INTEGER_ZERO)
                .eq(Note::getZhId, zhId)
                .eq(Note::getAccountingSetId, LoginUser.getLoginSetId())));
    }
}