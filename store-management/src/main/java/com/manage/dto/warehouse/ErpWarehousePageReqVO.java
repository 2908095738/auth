package com.manage.dto.warehouse;


import com.bbs.enums.CommonStatusEnum;
import com.bbs.validation.InEnum;
import com.bbs.vo.BaseParam;
import lombok.Data;

/**
 * 管理后台 - ERP 仓库分页
 */
@Data
public class ErpWarehousePageReqVO extends BaseParam {

    /**
     * 仓库名称
     */
    private String name;
    /**
     * 开启状态
     */
    @InEnum(CommonStatusEnum.class)
    private Integer status;

}