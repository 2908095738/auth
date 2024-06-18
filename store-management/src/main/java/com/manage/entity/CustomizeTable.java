package com.manage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 自定义表
 * @TableName customize_table
 */
@TableName(value ="customize_table")
@Data
public class CustomizeTable implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 列名
     */
    @TableField(value = "name")
    private String name;

    /**
     * 属性名
     */
    @TableField(value = "property")
    private String property;

    /**
     * 属性类型
     */
    @TableField(value = "property_type")
    private String propertyType;

    /**
     * 仓库表 id
     */
    @TableField(value = "ware_house_id")
    private Long wareHouseId;

    /**
     * 输入方式（单选 / 文本框 /…）
     */
    @TableField(value = "input_type")
    private String inputType;

    /**
     * 选项（单选多选的选项）
     */
    @TableField(value = "options")
    private String options;

    /**
     * 排序
     */
    @TableField(value = "sort")
    private Integer sort;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}