package com.manage.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.vo.BaseParam;
import com.manage.entity.CustomizeTable;
import com.manage.service.CustomizeTableService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * 管理后台 - 自定义表
 */
@RestController
@RequestMapping("/customize/table")
public class CustomizeTableController {


    @Resource
    private CustomizeTableService customizeTableService;


    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class QueryCustomizeTable extends BaseParam {

        private Long wareHouseId;

    }


    /**
     * 查询自定义表 列表
     * @return Long
     */
    @GetMapping()
    public Result<Page<CustomizeTable>> getWarehouse(@Valid @RequestBody QueryCustomizeTable param) {
        return Result.success(customizeTableService.getPageBy(param));
    }



    /**
     * 查询自定义表
     * @param id 主键
     * @return Long
     */
    @GetMapping()
    public Result<CustomizeTable> getWarehouse(@NotNull Long id) {
        return Result.success(customizeTableService.getById(id));
    }


    @Data
    public static class CustomizeTableParam{

        /**
         * 列名
         */
        private String name;
        /**
         * 属性名
         */
        private String property;

        /**
         * 属性类型
         */
        private String propertyType;

        /**
         * 仓库表 id
         */
        private Long wareHouseId;

        /**
         * 输入方式（单选 / 文本框 /…）
         */
        private String inputType;

        /**
         * 选项（单选多选的选项）
         */
        private String options;

        /**
         * 排序
         */
        private Integer sort;


    }

    /**
     * 创建自定义表
     * @return Long
     */
    @PostMapping()
    public Result<Long> createWarehouse(@Valid @RequestBody CustomizeTableParam param) {
        return Result.success(customizeTableService.create(param));
    }

    /**
     * 更新自定义表
     * @return Boolean
     */
    @PutMapping()
    public Result<Boolean> updateWarehouse(@Valid @RequestBody CustomizeTableParam param) {
        return Result.success(customizeTableService.change(param));
    }

    /**
     * 删除自定义表
     * @return Boolean
     */
    @DeleteMapping()
    public Result<Boolean> deleteWarehouse(@NotNull Long id) {
        return Result.success(customizeTableService.removeById(id));
    }



}
