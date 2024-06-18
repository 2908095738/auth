package com.manage.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.manage.controller.CustomizeTableController;
import com.manage.entity.CustomizeTable;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 *
 */
public interface CustomizeTableService extends IService<CustomizeTable> {

    Long create(CustomizeTableController.CustomizeTableParam param);

    Boolean change(CustomizeTableController.CustomizeTableParam param);

    Page<CustomizeTable> getPageBy(CustomizeTableController.QueryCustomizeTable param);
}
