package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.bbs.financial.util.LoginUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;

/**
 * 日记账
 * @TableName ledger_subsidiary
 */
@TableName(value ="ledger_subsidiary")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LedgerSubsidiary extends Model<LedgerSubsidiary> implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 凭证 ID
     */
    @TableField(value = "certificate_id")
    private Long certificateId;

    /**
     * 凭证字号
     */
    @TableField(value = "certificate_number")
    private String certificateNumber;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public LedgerSubsidiary(Account account, Certificate certificate, CertificateAbstract certificateAbstract, Long borrowMoney, Long loansMoney) {
        this.certificateId = certificateAbstract.getCertificateId();
        this.certificateNumber = certificate.getCertificateWord().name() + certificate.getNo();
        this.certificateAbstract = certificateAbstract.getCertificateAbstract();
        this.borrowMoney = certificateAbstract.getBorrowMoney();
        this.loansMoney = certificateAbstract.getLoansMoney();

        String direction = account.getDirection();
        if(StringUtils.isNotBlank(direction)) {
            this.directionBorrowing = direction.equals("借") ? INTEGER_ZERO : INTEGER_ONE;
        } else {
            this.directionBorrowing = borrowMoney > INTEGER_ZERO ? INTEGER_ZERO : INTEGER_ONE;
        }

        this.balance = Objects.equals(directionBorrowing, INTEGER_ZERO) ? borrowMoney : loansMoney;
        this.createBy = LoginUser.getId();
    }

    public static void createLedgerSubsidiary(Account account, Certificate certificate, CertificateAbstract certificateAbstract, Long borrowMoney, Long loansMoney) {
        LedgerSubsidiary ledgerSubsidiary = new LedgerSubsidiary(account, certificate, certificateAbstract, borrowMoney, loansMoney);
        ledgerSubsidiary.insert();
    }
}