package com.bbs.financial.entity;

import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;

/**
 * 科目
 * @TableName account_2
 */
@TableName(value ="account_2")
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
public class Account extends Model<Account> implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 科目类别
     */
    @TableField(value = "account_sort")
    private String accountSort;

    /**
     * 编号
     */
    @TableField(value = "no")
    private String no;

    /**
     * 会计科目名称
     */
    @TableField(value = "account_name")
    private String accountName;

    /**
     * 名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 类别
     */
    @TableField(value = "sort")
    private String sort;

    /**
     * 方向
     */
    @TableField(value = "direction")
    private String direction;

    /**
     * 是否外币核算
     */
    @TableField(value = "currency")
    private String currency;

    /**
     * 是否期末调汇
     */
    @TableField(value = "period_exchange_rate_adjust")
    private String periodExchangeRateAdjust;

    /**
     * 辅助核算类型id
     */
    @TableField(value = "account_auxiliary_type_ids")
    private String accountAuxiliaryTypeIds;

    /**
     * 是否现金支付
     */
    @TableField(value = "cash_pay")
    private String cashPay;

    /**
     * 是否数量核算
     */
    @TableField(value = "quantitative_account")
    private String quantitativeAccount;


    /**
     * 公司ID
     */
    @TableField(value = "accounting_set_id")
    private Long accountingSetId;

    /**
     * 上级ID
     */
    @TableField(value = "parent_id")
    private Long parentId;

    /**
     * 上级ID集合（String，逗号分隔）
     */
    @TableField(value = "parent_ids")
    private String parentIds;

    /**
     * 权重
     */
    @TableField(value = "weight")
    private Integer weight;

    /**
     * 级别
     */
    @TableField(value = "level")
    private Integer level;

    /**
     * 数量核算: 计量单位
     */
    @TableField(value = "measurement_unit")
    private String measurementUnit;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableField(exist = false)
    private AccountRemark remark;

    @TableField(exist = false)
    private List<AccountAuxiliary> accountAuxiliaryList;

    public static Map<Long, Account> converterToIdMap(List<Account> accounts) {
        return accounts.stream().collect(Collectors.toMap(Account::getId, account -> account));
    }



    public static List<Long> getParents(Account account) {
        List<String> ids = JSONArray.parseArray(account.getParentIds(), String.class);
        List<String> notContainCurrentAccountIdList = ids.subList(INTEGER_ZERO, ids.size());
        return notContainCurrentAccountIdList.stream()
                .map(String::trim)
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    public static List<Long> getParents(String idJSONArr) {
        List<String> ids = JSONArray.parseArray(idJSONArr, String.class);
        List<String> notContainCurrentAccountIdList = ids.subList(INTEGER_ZERO, ids.size());
        return notContainCurrentAccountIdList.stream()
                .map(String::trim)
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }
}