package com.bbs.financial.api.note.add;

import com.bbs.Result;
import com.bbs.enums.CodeEnum;
import com.bbs.exception.BusinessException;
import com.bbs.financial.api.certificate.add.AddCertificate;
import com.bbs.financial.api.certificate.no.search.SearchCertificateNo;
import com.bbs.financial.api.note.search.SearchOriByZh;
import com.bbs.financial.controller.CertificateController;
import com.bbs.financial.entity.Note;
import com.bbs.financial.entity.ZhangHu;
import com.bbs.financial.service.NoteService;
import com.bbs.financial.service.ZhangHuService;
import com.bbs.financial.util.LoginUser;
import com.bbs.financial.util.ORMUtil;
import com.bbs.financial.util.SpringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

import static org.apache.commons.lang3.math.NumberUtils.*;

/**
 * 初始金额新增凭证
 */
@RestController
@RequestMapping
public class AddCertByOri {

    @Resource
    private ApplicationContext appContext;

    @Resource
    private DataSourceTransactionManager transactionManager;

    @Resource
    private TransactionDefinition transactionDefinition;

    @Resource
    private NoteService noteORM;

    @Resource
    private ZhangHuService zhORM;

    private class StringTip {
        public static final String ABST = "初始金额";

        public static final String DEL_INIT_CERT_FAIL = "删除初始金额日记账的已有凭证失败";

        public static final String ADD_INIT_CERT_FAIL = "生成初始金额日记账的凭证失败";
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {
        /**
         * 开票日期
         */
        private Date openDate;

        /**
         * 是否有科目
         */
        private Boolean hasSubj;

        /**
         * 账户id
         */
        private Long zhId;

        private List<DetailParam> details;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailParam {
        /**
         * 摘要
         */
        private String certificateAbstract;

        /**
         * 科目
         */
        private Long accountId;

        /**
         * 借方金额
         */
        private String borrowMoney;

        /**
         * 贷方金额
         */
        private String loansMoney;

        /**
         * 权重
         */
        private String weight;
    }

    @PutMapping("/note/ori/cert")
    public Result<Boolean> addCert(@RequestBody Param param) {
        Note toDBNote = getOriNote2DB(param.getZhId(),
                param.getDetails().get(INTEGER_ZERO).getLoansMoney(),
                param.getOpenDate(),
                param.getDetails().get(INTEGER_ONE).getAccountId());

        boolean isHasNote = Objects.nonNull(toDBNote.getId());
        if (!isDelCert(isHasNote, toDBNote.getCertificateId()))
            return Result.failed(StringTip.DEL_INIT_CERT_FAIL);

        Result<Long> idResult = getCertId(param.getOpenDate(), param.getDetails());
        if (idResult.getCode().equals(CodeEnum.SUCCESS_USER_LOGIN.getCode()))
            toDBNote.setCertificateId(idResult.getData());
        else
            return Result.failed(idResult.getMsg());

        return ORMUtil.fastTran(() -> {
            if (!param.getHasSubj()) {//账户没有设置科目，用初始金额凭证摘要中的科目赋值
                Long subjId = param.getDetails().get(INTEGER_ZERO).getAccountId();
                zhORM.lambdaUpdate()
                        .set(ZhangHu::getSubjectsId, subjId)
                        .eq(ZhangHu::getId, param.getZhId())
                        .update();
            }

            if (isHasNote)
                noteORM.lambdaUpdate()
                        .set(Note::getHeAccountId, toDBNote.getHeAccountId())
                        .set(Note::getBorrowMoney, toDBNote.getBorrowMoney())
                        .set(Note::getCertificateId, toDBNote.getCertificateId())
                        .set(Note::getUpdateBy, LoginUser.getId())
                        .eq(Note::getId, toDBNote.getId())
                        .update();
            else
                noteORM.save(toDBNote);
        }, transactionManager, transactionDefinition);
    }

    /**
     * 获取待入库的初始金额日记账
     *
     * @param zhId     账户id
     * @param moneyStr 初始金额字符串
     * @param openDate 开票日期
     * @param heSubjId 对方科目id
     */
    private Note getOriNote2DB(Long zhId, String moneyStr, Date openDate, Long heSubjId) {
        Note toDBNote = SpringUtil.getRespData(SearchOriByZh.class, appContext, s -> s.search(zhId));
        boolean isHasNote = Objects.nonNull(toDBNote);
        if (!isHasNote) {
            BigDecimal money = new BigDecimal(moneyStr);
            toDBNote = getOriNoteByCreate(money, openDate, zhId);
        } else
            toDBNote.setBorrowMoney(new BigDecimal(moneyStr));

        toDBNote.setHeAccountId(heSubjId);
        return toDBNote;
    }

    /**
     * 获取后端生成的待入库初始金额日记账
     *
     * @param money    初始金额
     * @param openDate 开票日期
     * @param zhId     账户id
     */
    private Note getOriNoteByCreate(BigDecimal money, Date openDate, Long zhId) {
        Note toDBNote = new Note();

        toDBNote.setCertificateAbstract(StringTip.ABST);
        toDBNote.setBorrowMoney(money);
        toDBNote.setDate(openDate);
        toDBNote.setCreateBy(LoginUser.getId());
        toDBNote.setNoteType(INTEGER_ZERO);
        toDBNote.setZhId(zhId);
        toDBNote.setAccountingSetId(LoginUser.getLoginSetId());

        return toDBNote;
    }

    /**
     * 删除已有凭证
     *
     * @param isHasNote 日记账是否存在
     * @param certId    凭证id
     */
    private boolean isDelCert(boolean isHasNote, Long certId) {
        if (isHasNote && Objects.nonNull(certId)) {
            boolean isDone = SpringUtil.getRespData(CertificateController.class, appContext, c -> c.remove(Collections.singletonList(certId)));
            if (!isDone)
                return false;
        }
        return true;
    }

    /**
     * 获取凭证id
     *
     * @param openDate 开票日期
     * @param details  凭证摘要列表
     */
    private Result<Long> getCertId(Date openDate, List<DetailParam> details) {
        AddCertificate.Param certParam = getCertParam(openDate, details);

        Object addRespData = null;
        try {
            addRespData = SpringUtil.getRespData(AddCertificate.class, appContext, a -> a.add(certParam));
        } catch (RuntimeException e) {
            if (e.getCause() instanceof BusinessException)
                return Result.failed(((BusinessException) e.getCause()).getMsg());
            throw e;
        }

        if (Objects.isNull(addRespData) || !(addRespData instanceof Long))
            return Result.failed(StringTip.ADD_INIT_CERT_FAIL);

        Long certId = (Long) addRespData;
        return Result.success(certId);
    }

    /**
     * 获取生成凭证所需参数
     *
     * @param openDate   开票日期
     * @param detailList 发票明细摘要列表
     */
    private AddCertificate.Param getCertParam(Date openDate, List<DetailParam> detailList) {
        AddCertificate.Param param = new AddCertificate.Param();

        param.setAccountingSetId(LoginUser.getLoginSetId());
        param.setCertificateWord(NumberUtils.INTEGER_ZERO + "");
        param.setNo(getNo(openDate));
        param.setDate(openDate);
        param.setIsNeedCertId(true);
        param.setAbstracts(getAbst(detailList));

        return param;
    }

    /**
     * 获取编号
     *
     * @param date 日期
     */
    private Long getNo(Date date) {
        SearchCertificateNo.Param reqParam = new SearchCertificateNo.Param();
        reqParam.setDate(date.getTime());

        return SpringUtil.getRespData(SearchCertificateNo.class, appContext, c -> c.search(reqParam));
    }

    /**
     * 获取凭证摘要列表
     *
     * @param detailList 发票明细摘要列表
     */
    private List<AddCertificate.Abstract> getAbst(List<DetailParam> detailList) {
        List<AddCertificate.Abstract> result = new ArrayList<>();

        for (DetailParam detail : detailList) {
            AddCertificate.Abstract abst = new AddCertificate.Abstract();

            abst.setCertificateAbstract(detail.getCertificateAbstract());
            abst.setAccountId(detail.getAccountId());

            if (StringUtils.isNotBlank(detail.getBorrowMoney())) {
                BigDecimal oriMoney = new BigDecimal(detail.getBorrowMoney());
                BigDecimal doneMoney = oriMoney.setScale(NumberUtils.INTEGER_ZERO, BigDecimal.ROUND_DOWN);
                abst.setBorrowMoney(doneMoney.toString());
            }
            if (StringUtils.isNotBlank(detail.getLoansMoney())) {
                BigDecimal oriMoney = new BigDecimal(detail.getLoansMoney());
                BigDecimal doneMoney = oriMoney.setScale(NumberUtils.INTEGER_ZERO, BigDecimal.ROUND_DOWN);
                abst.setLoansMoney(doneMoney.toString());
            }

            result.add(abst);
        }

        return result;
    }
}