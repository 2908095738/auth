package com.bbs.financial.api.note.delete;

import com.bbs.Result;
import com.bbs.financial.api.certificate.delete.DelCertificate;
import com.bbs.financial.entity.Note;
import com.bbs.financial.enums.CertTypeEnum;
import com.bbs.financial.service.NoteService;
import com.bbs.financial.service.ZhangHuService;
import com.bbs.financial.util.ORMUtil;
import com.bbs.financial.util.SpringUtil;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 删除日记账下的账户
 */
@RestController
@RequestMapping
public class DeleteZh {
    @Resource
    private TransactionDefinition tranDef;

    @Resource
    private DataSourceTransactionManager tranManager;

    @Resource
    private ApplicationContext appContext;

    @Resource
    private NoteService noteORM;

    @Resource
    private ZhangHuService zhORM;

    private class StringTip {
        public static final String NO_ZH = "请传入账户id列表";

        public static final String FAIL_DEL_CERT = "删除日记账的凭证失败";

        public static final String FAIL_DEL_NOTE = "删除日记账失败";
    }

    @DeleteMapping("/cashier/zhanghu/batch/{idList}")
    public Result<Boolean> remove(@PathVariable List<Long> idList) {
        //非空校验
        if (ObjectUtils.isEmpty(idList))
            return Result.failed(StringTip.NO_ZH);

        //查询账户的初始金额日记账列表
        List<Note> noteList = noteORM.selectJoinList(Note.class,
                new MPJLambdaWrapper<Note>()
                        .select(Note::getId, Note::getCertificateId)
                        .in(Note::getZhId, idList));

        //初始化凭证、日记账id列表
        List<Long> certIdList = new ArrayList<>();
        List<Long> noteIdList = new ArrayList<>();
        for (Note note : noteList) {
            if (Objects.nonNull(note.getCertificateId()))
                certIdList.add(note.getCertificateId());
            noteIdList.add(note.getId());
        }

        //删除凭证
        boolean isDone = false;
        if (!certIdList.isEmpty()) {
            List<DelCertificate.DelParam> delList = certIdList.stream()
                    .map(id -> new DelCertificate.DelParam(CertTypeEnum.NONE.getType(), id))
                    .collect(Collectors.toList());
            isDone = SpringUtil.getRespData(DelCertificate.class, appContext, c -> c.remove(delList));
            if (!isDone)
                return Result.failed(StringTip.FAIL_DEL_CERT);
        }

        //删除初始金额日记账
        if (!noteIdList.isEmpty()) {
            isDone = SpringUtil.getRespData(DeleteNote.class, appContext, d -> d.remove(noteIdList));
            if (!isDone)
                return Result.failed(StringTip.FAIL_DEL_NOTE);
        }

        //删除账户
        return ORMUtil.fastTran(() -> zhORM.removeBatchByIds(idList), tranManager, tranDef);
    }
}