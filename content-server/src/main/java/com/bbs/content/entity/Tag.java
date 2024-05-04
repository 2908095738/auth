package com.bbs.content.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 标签
 * @TableName tag
 */
@TableName(value ="tag")
@Data
@Accessors(chain = true)
public class Tag implements Serializable {
    /**
     * 编号
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 标签名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 权重 =P表示热度因子(评论数+点赞数+浏览数)；T表示距离发帖的时间（单位为小时）；G表示"重力因子"（gravityth power），即将帖子排名往下拉的力量，默认值为1.8;  权重小于0时表示强制下沉，不再在热门榜上显示;  超出时间限制的热门话题权重在重新计算时设置为0        KaTeX公式表示：Score = \dfrac{P-1}{(T+2)^G}
     */
    @TableField(value = "weight")
    private Double weight;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}