package com.bbs.mall.bo;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@ApiOperation("促销商品")
public class LowProductBO {
    List<LowSkuBO> skus;

    List<LowLadderBO> ladders;

    List<LowReduceBO> reduces;

    @ApiModelProperty(value = "商品id")
    private Long id;

    @ApiModelProperty(value = "促销类型：0->没有促销使用原价;1->使用促销价；2->待定；3->使用阶梯价格；4->使用满减价格；5->限时购")
    private Integer lowType;

    @Data
    @ApiOperation("促销SKU")
    public class LowSkuBO {
        @ApiModelProperty("促销商品SKUid")
        private Long skuId;

        @ApiModelProperty(value = "促销商品SKU编码")
        private String skuCode;

        @ApiModelProperty(value = "商品SKU价格")
        private BigDecimal price;

        @ApiModelProperty(value = "促销商品SKU价格")
        private BigDecimal lowPrice;

        @ApiModelProperty(value = "促销商品SKU库存")
        private Integer stock;

        @ApiModelProperty(value = "促销商品SKU锁定库存")
        private Integer lockStock;
    }

    @Data
    @ApiOperation("促销商品阶梯价格")
    public class LowLadderBO {
        @ApiModelProperty(value = "促销商品阶梯价格id")
        private Long ladderId;

        @ApiModelProperty(value = "满足的商品数量")
        private Integer count;

        @ApiModelProperty(value = "折扣")
        private BigDecimal discount;
    }

    @Data
    @ApiOperation("促销商品满减价格")
    public class LowReduceBO {
        @ApiModelProperty(value = "促销商品满减id")
        private Long reduceId;

        @ApiModelProperty(value = "满足条件价格")
        private BigDecimal fullPrice;

        @ApiModelProperty(value = "满减差价")
        private BigDecimal reducePrice;
    }
}