package com.bbs.financial.api.note.add;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.api.certificate.add.AddCertificate;
import com.bbs.financial.api.certificate.no.search.SearchCertificateNo;
import com.bbs.financial.api.certificate.search.SearchCertificate;
import com.bbs.financial.controller.CheckoutController;
import com.bbs.financial.entity.Certificate;
import com.bbs.financial.entity.Note;
import com.bbs.financial.entity.ZhangHu;
import com.bbs.financial.service.NoteService;
import com.bbs.financial.util.SpringUtil;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.apache.commons.lang3.math.NumberUtils.*;

@RestController
@RequestMapping
public class AddCertByNote {
    @Resource
    private DataSourceTransactionManager transactionManager;

    @Resource
    private TransactionDefinition transactionDefinition;

    @Resource
    private NoteService orm;

    @Resource
    private ApplicationContext applicationContext;

    private final class StringTIP {
        private StringTIP() {
            throw new RuntimeException("stop create obj");
        }

        public static final String NO_NOTE = "未找到日记账";

        public static final String NO_MONEY = "当前日记账无收入/支出";

        public static final String IS_CHECK = "已结账";

        public static final String NO_ZH = "未找到账户";

        public static final String NO_SUBJ = "未找到账户对方科目/账户科目";

        public static final String FAIL_CERT = "添加凭证失败";
    }

    /**
     * 日记账生成凭证
     *
     * @param id 日记账id
     */
    @GetMapping("/note/cert/{id}")
    public Result<Boolean> addCert(@PathVariable Long id) {
        //非空校验
        Note tmpNote = getNote(id);
        if (ObjectUtils.isEmpty(tmpNote)) return Result.failed(StringTIP.NO_NOTE);

        if (ObjectUtils.isEmpty(tmpNote.getBorrowMoney()) && ObjectUtils.isEmpty(tmpNote.getLoansMoney()))
            return Result.failed(StringTIP.NO_MONEY);

        //是否结账
        CheckoutController checkoutController = (CheckoutController) applicationContext.getBean(SpringUtil.getClassNameOfFirstLow(CheckoutController.class));
        boolean isCheck = checkoutController.isCheck(tmpNote.getDate().getTime() + "").getData();
        if (isCheck) return Result.failed(StringTIP.IS_CHECK);

        //非空校验
        ZhangHu tmpZh = tmpNote.getZhangHu();
        if (ObjectUtils.isEmpty(tmpZh)) return Result.failed(StringTIP.NO_ZH);
        if (ObjectUtils.isEmpty(tmpNote.getHeAccountId()) || ObjectUtils.isEmpty(tmpZh.getSubjectsId()))
            return Result.failed(StringTIP.NO_SUBJ);

        //新增凭证
        AddCertificate.Param addParam = getCertParam(tmpNote);
        AddCertificate addCertController = (AddCertificate) applicationContext.getBean(SpringUtil.getClassNameOfFirstLow(AddCertificate.class));
        boolean isAdd = (boolean) addCertController.add(addParam).getData();
        if (!isAdd) return Result.failed(StringTIP.FAIL_CERT);

        //日记账修改
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            orm.lambdaUpdate().set(Note::getCertificateId, getCertId(addParam)).eq(Note::getId, tmpNote.getId()).update();

            transactionManager.commit(transaction);
            return Result.success();
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取日记账
     *
     * @param id 日记账id
     */
    private Note getNote(Long id) {
        return orm.selectJoinOne(Note.class, new MPJLambdaWrapper<Note>().eq(Note::getId, id).selectAssociation(ZhangHu.class, Note::getZhangHu).leftJoin(ZhangHu.class, ZhangHu::getId, Note::getZhId));
    }

    /**
     * 获取生成凭证所需参数
     */
    private AddCertificate.Param getCertParam(Note tmpNote) {
        AddCertificate.Param param = new AddCertificate.Param();
        param.setCertificateWord(NumberUtils.INTEGER_ZERO + "");
        param.setNo(getNo(tmpNote.getDate()));
        param.setDate(tmpNote.getDate());
        param.setAbstracts(getAbst(tmpNote));
        return param;
    }

    /**
     * 获取编号
     *
     * @param date 日期
     */
    private Long getNo(Date date) {
        SearchCertificateNo getCertNoController = (SearchCertificateNo) applicationContext.getBean(SpringUtil.getClassNameOfFirstLow(SearchCertificateNo.class));
        SearchCertificateNo.Param tmpParam = new SearchCertificateNo.Param();
        tmpParam.setDate(date.getTime());
        return getCertNoController.search(tmpParam).getData();
    }

    /**
     * 获取凭证摘要列表
     */
    private List<AddCertificate.Abstract> getAbst(Note tmpNote) {
        List<AddCertificate.Abstract> result = new ArrayList<>();

        //第一个凭证摘要初始化
        AddCertificate.Abstract firstAbst = new AddCertificate.Abstract();
        firstAbst.setCertificateAbstract(tmpNote.getCertificateAbstract());
        firstAbst.setAccountId(tmpNote.getZhangHu().getSubjectsId());
        firstAbst.setBorrowMoney(ObjectUtils.isEmpty(tmpNote.getBorrowMoney()) ? null : tmpNote.getBorrowMoney().longValue() + "");
        firstAbst.setLoansMoney(ObjectUtils.isEmpty(tmpNote.getLoansMoney()) ? null : tmpNote.getLoansMoney().longValue() + "");

        //第二个凭证摘要初始化
        AddCertificate.Abstract endAbst = new AddCertificate.Abstract();
        firstAbst.setCertificateAbstract(tmpNote.getCertificateAbstract());
        endAbst.setAccountId(tmpNote.getHeAccountId());
        if (StringUtils.isNotBlank(firstAbst.getBorrowMoney())) endAbst.setLoansMoney(firstAbst.getBorrowMoney());
        if (StringUtils.isNotBlank(firstAbst.getLoansMoney())) endAbst.setBorrowMoney(firstAbst.getLoansMoney());

        result.add(firstAbst);
        result.add(endAbst);

        return result;
    }

    /**
     * 获取凭证id
     */
    private Long getCertId(AddCertificate.Param addParam) {
        SearchCertificate getCertController = (SearchCertificate) applicationContext.getBean(SpringUtil.getClassNameOfFirstLow(SearchCertificate.class));
        Result<Page<Certificate>> tmpResult = getCertController.search(1, 1, Collections.singletonList(addParam.getCertificateWord()), addParam.getNo(), null, null, DateUtil.beginOfMonth(addParam.getDate()).getTime(), DateUtil.beginOfMonth(DateUtil.offsetMonth(addParam.getDate(), INTEGER_ONE)).getTime());
        return tmpResult.getData().getRecords().get(0).getId();
    }
}