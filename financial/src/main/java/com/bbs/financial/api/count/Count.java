package com.bbs.financial.api.count;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bbs.Result;
import com.bbs.financial.api.count.vo.CountVO;
import com.bbs.financial.entity.Note;
import com.bbs.financial.service.NoteService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

/**
 * 首页
 */
@RequestMapping
@RestController
public class Count {
    @Resource
    private NoteService noteService;

    public Result<CountVO> count() {
        Map<Long, List<Note>> accountIdMap = noteService.list(new LambdaQueryWrapper<Note>()
                .orderByDesc(Note::getCreateTime)
                .first("limit 1")
        ).stream().collect(Collectors.groupingBy(Note::getZhId));
        accountIdMap.entrySet().stream().map(entry -> {
            BigDecimal count = BigDecimal.ZERO;
            for (Note note: entry.getValue()) {
                BigDecimal borrowMoney = note.getBorrowMoney();
                count = nonNull(borrowMoney) ? count.add(borrowMoney) : count.subtract(note.getLoansMoney());
            }
            return count;
        });
        return null;
    }
}
