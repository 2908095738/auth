package com.bbs.financial.api.accountBook;

import cn.hutool.core.collection.CollUtil;
import com.bbs.Result;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.service.CertificateAbstractService;
import com.bbs.financial.util.LoginUser;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 账簿-数量金额明细账
 */
@RestController
public class QuantityController {


    @Resource
    private CertificateAbstractService certificateAbstractService;

    @GetMapping("/certificate/account/quantity")
    public Result<List<Vo>> quantityAccount(@RequestParam("createTime") String certificateCreateTime,
                                            @RequestParam("accountId")Long accountId) {
        List<CertificateAbstract> list = certificateAbstractService.selectQuantityAmountList(LoginUser.getCompanyId(), certificateCreateTime, accountId);
        List<Vo> result = new ArrayList<>();
        if(CollUtil.isNotEmpty(list)){
            certificateAbstractService.initDataByMonth(list);
            for (CertificateAbstract anAbstract : list) {
                Vo vo = new Vo();
                vo.setAccountId(anAbstract.getAccountId());
                vo.setCertificateAbstract(anAbstract.getCertificateAbstract());
                if(Objects.nonNull(anAbstract.getAccountAuxiliary())){
                    vo.setAccountName(anAbstract.getAccountAuxiliary().getName());
                }else{
                    vo.setAccountName(anAbstract.getAccount().getName());
                }
                if(Objects.nonNull(anAbstract.getCertificate())){
                    vo.setCreateTime(anAbstract.getCertificate().getCreateTime());
                    vo.setCertificateWord(anAbstract.getCertificate().getCertificateWord().getMsg());
                    vo.setNo(anAbstract.getCertificate().getNo());
                }else{
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM");
                    try {
                        Date date = inputFormat.parse(certificateCreateTime);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                        vo.setCreateTime(calendar.getTime());
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
                if(Objects.nonNull(anAbstract.getLoansMoney())){
                    vo.setBorrowNum(anAbstract.getNum());
                    vo.setBorrowPrice(anAbstract.getPrice());
                    vo.setBorrowMoney(anAbstract.getLoansMoney());
                }else if (Objects.nonNull(anAbstract.getBorrowMoney())){
                    vo.setLoansNum(anAbstract.getNum());
                    vo.setLoansPrice(anAbstract.getPrice());
                    vo.setLoansMoney(anAbstract.getBorrowMoney());
                }else{
                    vo.setSurplusNum(anAbstract.getNum());
                    vo.setSurplusPrice(anAbstract.getPrice());
                    vo.setSurplusMoney(anAbstract.getSurplusMoney());
                }
                result.add(vo);
            }
        }
        return Result.success(result);
    }



    @Data
    private static class Vo {

        /**
         * 科目id
         */
        private Long accountId;

        /**
         * 日期
         */
        private Date createTime;

        /**
         * 凭证字
         */
        private String certificateWord;

        /**
         * 凭证编号
         */
        private Long no;

        private String certificateAbstract;

        /**
         * 科目名称
         */
        private String accountName;




        /**
         * 借方发生额-数量
         */
        private Long borrowNum;

        /**
         * 借方发生额-单价
         */
        private Long borrowPrice;

        /**
         * 借方发生额-金额
         */
        private Long borrowMoney;




        /**
         * 贷方发生额-数量
         */
        private Long loansNum;

        /**
         * 贷方发生额-单价
         */
        private Long loansPrice;

        /**
         * 贷方发生额-金额
         */
        private Long loansMoney;




        /**
         * 余额-数量
         */
        private Long surplusNum;

        /**
         * 余额-单价
         */
        private Long surplusPrice;

        /**
         * 余额-金额
         */
        private Long surplusMoney;



    }

}
