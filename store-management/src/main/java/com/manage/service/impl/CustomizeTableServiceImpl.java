package com.manage.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.util.BeanUtils;
import com.manage.controller.CustomizeTableController;
import com.manage.entity.CustomizeTable;
import com.manage.mapper.CustomizeTableMapper;
import com.manage.service.CustomizeTableService;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 *
 */
@Service
public class CustomizeTableServiceImpl extends ServiceImpl<CustomizeTableMapper, CustomizeTable>
    implements CustomizeTableService{


    @Override
    public Long create(CustomizeTableController.CustomizeTableParam param) {
        CustomizeTable bean = BeanUtils.toBean(param, CustomizeTable.class);
        save(bean);
        return bean.getId();
    }

    @Override
    public Boolean change(CustomizeTableController.CustomizeTableParam param) {
        CustomizeTable bean = BeanUtils.toBean(param, CustomizeTable.class);
        return updateById(bean);
    }

    @Override
    public Page<CustomizeTable> getPageBy(CustomizeTableController.QueryCustomizeTable param) {
        return lambdaQuery()
                .eq(Objects.nonNull(param.getWareHouseId()),CustomizeTable::getWareHouseId,param.getWareHouseId())
                .page(new Page<>(param.getCurrent(),param.getSize()));

    }


}




