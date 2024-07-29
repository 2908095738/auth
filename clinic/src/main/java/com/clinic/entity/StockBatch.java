package com.clinic.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.clinic.dto.PrescriptionDrugDto;
import com.clinic.enums.DrugStockRule;
import com.clinic.enums.DrugTypeEnum;
import com.clinic.enums.StockStateEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 
 * @TableName stock_batch
 */
@TableName(value ="stock_batch")
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class StockBatch implements Serializable {

    /**
     * 编号
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 库存编号
     */
    @TableField(value = "stock_id")
    private Long stockId;

    /**
     * 批注文号
     */
    @TableField(value = "approval_number")
    private String approvalNumber;

    /**
     * 厂商名称
     */
    @TableField(value = "manufacturer")
    private String manufacturer;

    /**
     * 批次号
     */
    @TableField(value = "batch_number")
    private String batchNumber;

    /**
     * 生产日期
     */
    @TableField(value = "produce_date")
    private Date produceDate;

    /**
     * 过期日期
     */
    @TableField(value = "expiry_date")
    private Date expiryDate;

    /**
     * 过期状态（0正常、1过期）
     */
    @TableField(value = "expiry_state")
    private Integer expiryState;

    /**
     * 剂型
     */
    @TableField(value = "dosage_form")
    private String dosageForm;

    /**
     * 规格
     */
    @TableField(value = "spec")
    private String spec;

    /**
     * 药品类型，取国药准字首字母
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 药品类型
     */
    @TableField(exist = false)
    private DrugTypeEnum typeObj;

    /**
     * 国家基本药物类型
     */
    @TableField(value = "essential")
    private Integer essential;

    /**
     * 皮试（0非皮试；1皮试）
     */
    @TableField(value = "skin_test")
    private Integer skinTest;

    /**
     *售价
     */
    @TableField(value = "price")
    private BigDecimal price;

    /**
     * 用法
     */
    @TableField(value = "drug_usage")
    private String drugUsage;

    /**
     * 单次剂量
     */
    @TableField(value = "single_dose")
    private Integer singleDose;

    /**
     * 单次剂量单位
     */
    @TableField(value = "single_dose_unit")
    private Integer singleDoseUnit;

    /**
     * 单次剂量单位
     */
    @TableField(exist = false)
    private Unit singleDoseUnitObj;

    /**
     * 频次
     */
    @TableField(value = "frequency")
    private String frequency;

    /**
     * 库存量
     */
    @TableField(value = "number")
    private Long number;

    /**
     * 库存单位（编号，最小计数单位）
     */
    @TableField(value = "unit_id")
    private Integer unitId;

    /**
     * 库存单位（编号，最小计数单位）
     */
    @TableField(exist = false)
    private Unit unit;

    /**
     * 总数（最近一次入库时候的存量 + 入库量）
     */
    @TableField(value = "total_number")
    private Long totalNumber;

    /**
     * 库存统计规则
     */
    @TableField(value = "state_count_rule")
    private DrugStockRule stateCountRule;

    /**
     * 统计值（统计方式值，如百分比 10%； 数量）
     */
    @TableField(value = "count_val")
    private Integer countVal;

    /**
     * 统计单位（编号）
     */
    @TableField(value = "count_unit_id")
    private Integer countUnitId;

    /**
     * 库存状态
     */
    @TableField(value = "state")
    private StockStateEnum state;

    /**
     * 用户 ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 库存
     */
    @TableField(exist = false)
    private Stock stock;

    /**
     * 药品名称
     */
    @TableField(exist = false)
    private String name;

    /**
     * 单位列表
     */
    @TableField(exist = false)
    private List<StockUnit> stockUnitList;

    @TableField(exist = false)
    private List<Integer> stockUnitIds;

    @TableField(exist = false)
    private List<Drug> drug;

    /**
     * 供应商
     */
    @TableField(exist = false)
    private String provider;

    /**
     * 单位名称
     */
    @TableField(exist = false)
    private String unitName;

    /**
     * 进价
     */
    @TableField(value = "cost")
    private BigDecimal cost;

    /**
     * 进价单位
     */
    @TableField(value = "cost_unit")
    private Long costUnitId;

    /**
     * 进价单位
     */
    @TableField(exist = false)
    private Unit costUnit;
    /**
     * 药品类型（字母）
     */
    @TableField(exist = false)
    private String typeName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 入库信息
     */
    @TableField(exist = false)
    private List<StockInDrug> stockInDrugList;

    public StockBatch(PrescriptionDrugDto drug, Long oldStockBatchNumber) {
        this.id = drug.getStockBatchId();
        this.number = oldStockBatchNumber-drug.getQuantity();
    }

    public StockBatch(Long id) {
        this.id = id;
    }


    public StockBatch(Long id, Long number) {
        this.id = id;
        this.number = number;
    }
}