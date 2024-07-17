package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.bbs.financial.enums.AccountAbstractEnum;
import com.bbs.financial.util.LoginUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import static cn.hutool.core.lang.Opt.ofNullable;
import static org.apache.commons.lang3.math.NumberUtils.*;
import static org.apache.commons.lang3.math.NumberUtils.LONG_ZERO;

/**
 * 总账
 * @TableName ledger_general
 */
@TableName(value ="ledger_general")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LedgerGeneral extends Model<LedgerGeneral> implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 科目 ID
     */
    @TableField(value = "account_id")
    private Long accountId;

    /**
     * 科目编码
     */
    @TableField(value = "account_no")
    private String accountNo;

    /**
     * 信息创建人
     */
    @TableField(value = "create_by")
    private Long createBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 信息修改人
     */
    @TableField(value = "update_by")
    private Long updateBy;

    /**
     * 修改时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 科目名称
     */
    @TableField(value = "account_name")
    private String accountName;

    /**
     * 摘要
     */
    @TableField(value = "certificate_abstract")
    private String certificateAbstract;

    /**
     * 借方金额
     */
    @TableField(value = "borrow_money")
    private Long borrowMoney;

    /**
     * 贷方金额
     */
    @TableField(value = "loans_money")
    private Long loansMoney;

    /**
     * 借贷方向（0借/1贷）
     */
    @TableField(value = "direction_borrowing")
    private Integer directionBorrowing;

    /**
     * 余额
     */
    @TableField(value = "balance")
    private Long balance;

    @TableField(value = "company_id")
    private Long companyId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public LedgerGeneral(Account account, String certificateAbstract, Long borrowMoney, Long loansMoney, Integer directionBorrowing, Long balance) {
        this.accountId = account.getId();
        this.accountNo = account.getNo();
        this.createBy = LoginUser.getId();
        this.accountName = account.getName();
        this.certificateAbstract = certificateAbstract;
        this.borrowMoney = borrowMoney;
        this.loansMoney = loansMoney;
        this.directionBorrowing = directionBorrowing;
        this.balance = balance;
    }


    public void updateAccountBalance(Long addBorrowMoney, long addLoansMoney) {
        borrowMoney = addBorrowMoney + ofNullable(borrowMoney).orElseGet(LONG_ZERO::longValue);
        loansMoney = addLoansMoney + ofNullable(loansMoney).orElseGet(LONG_ZERO::longValue);
        updateById();
    }

    public static void initAccountLedgerGeneral(Account account) {
        long borrowMoney = LONG_ZERO;
        long loansMoney = LONG_ZERO;
        String accountDirection = account.getDirection();
        Integer directionBorrowing;
        long balance;
        // 尝试通过科目信息获取借贷方向
        // 如果是借方科目，增加借方金额；如果是贷方科目，增加贷方金额。
        if(StringUtils.isNotBlank(accountDirection)) {
            if(accountDirection.equals("借")) {
                directionBorrowing = INTEGER_ZERO;
                balance = borrowMoney;
            } else {
                directionBorrowing = INTEGER_ONE;
                balance = loansMoney;
            }
        //通过借贷金额判断借贷方向
        } else if(borrowMoney > INTEGER_ZERO) { // 借
            directionBorrowing = INTEGER_ZERO;
            balance = borrowMoney;
        } else {    //贷
            directionBorrowing = INTEGER_ONE;
            balance = loansMoney;
        }
        LedgerGeneral ledgerGeneral = new LedgerGeneral(account, "本期合计", loansMoney, borrowMoney, directionBorrowing, balance);
        ledgerGeneral.insert();
    }
}