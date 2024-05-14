package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 资产配置
 * @TableName finance_config
 */
@TableName(value ="finance_config")
@Data
public class FinanceConfig implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 资产编码
     */
    @TableField(value = "finance_code")
    private String financeCode;

    /**
     * 资产名称
     */
    @TableField(value = "finance_name")
    private String financeName;

    /**
     * 资产类别id
     */
    @TableField(value = "fin_cate_id")
    private Long finCateId;

    /**
     * 使用部门id
     */
    @TableField(value = "depart_id")
    private Long departId;

    /**
     * 开始使用日期
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 数量
     */
    @TableField(value = "quantity")
    private Integer quantity;

    /**
     * 规格型号
     */
    @TableField(value = "specs_type")
    private String specsType;

    /**
     * 存放地点
     */
    @TableField(value = "store_address")
    private String storeAddress;

    /**
     * 使用人id
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 折旧方法状态：
     */
    @TableField(value = "old_status")
    private Integer oldStatus;

    /**
     * 预计使用年限
     */
    @TableField(value = "future_year")
    private Integer futureYear;

    /**
     * 原值
     */
    @TableField(value = "ori_price")
    private BigDecimal oriPrice;

    /**
     * 税额
     */
    @TableField(value = "tax")
    private BigDecimal tax;

    /**
     * 残值率
     */
    @TableField(value = "future_price")
    private BigDecimal futurePrice;

    /**
     * 减值准备
     */
    @TableField(value = "down_price_todo")
    private BigDecimal downPriceTodo;

    /**
     * 期初累计折旧
     */
    @TableField(value = "ori_total_price")
    private BigDecimal oriTotalPrice;

    /**
     * 月折旧额
     */
    @TableField(value = "mouth_old_price")
    private BigDecimal mouthOldPrice;

    /**
     * 固定资产科目
     */
    @TableField(value = "lock_finance")
    private String lockFinance;

    /**
     * 资产购入对方科目
     */
    @TableField(value = "finance_to_he")
    private String financeToHe;

    /**
     * 税金科目
     */
    @TableField(value = "tax_subjects")
    private String taxSubjects;

    /**
     * 累计折扣科目
     */
    @TableField(value = "total_finance")
    private String totalFinance;

    /**
     * 折旧费用科目
     */
    @TableField(value = "old_fin_sub")
    private String oldFinSub;

    /**
     * 资产清理科目
     */
    @TableField(value = "fin_clear_sub")
    private String finClearSub;

    /**
     * 减值准备科目
     */
    @TableField(value = "down_price_todo_sub")
    private String downPriceTodoSub;

    /**
     * 减值准备对方科目
     */
    @TableField(value = "down_price_todo_sub_to_he")
    private String downPriceTodoSubToHe;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}