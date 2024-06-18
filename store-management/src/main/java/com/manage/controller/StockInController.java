package com.manage.controller;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.util.BeanUtils;
import com.manage.dto.warehouse.ErpWarehouseRespVO;
import com.manage.entity.TableValue;
import com.manage.service.CustomizeTableService;
import com.manage.service.TableValueService;
import lombok.Data;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

/**
 * 管理后台 - 库存入库单
 */
@RestController
@RequestMapping("/stock/in")
@Validated
public class StockInController {

    @Resource
    private CustomizeTableService customizeTableService;
    @Resource
    private TableValueService tableValueService;

    @Data
    public static class StockInParam{
        /**
         * 内容（{property1 :value1,property2: value2}）
         */
        private Object value;

        /**
         * 自定义表 id
         */
        private Long tableId;

    }

    /**
     * 创建入库单
     * @param param
     * @return Long id
     */
    @PostMapping()
    public Result<Long> createStockIn(@Valid @RequestBody StockInParam param) {

        TableValue tableValue = BeanUtils.toBean(param, TableValue.class);

        boolean b = tableValueService.save(tableValue);



        return Result.success(tableValue.getId());
    }

    /**
     * 更新入库单
     * @param param
     * @return Long
     */
    @PutMapping()
    public Result<Boolean> updateStockIn(@Valid @RequestBody TableValue param) {
        return Result.success(true);
    }

    /**
     * 更新入库单的状态
     * @param id
     * @param status
     * @return Boolean
     */
    @PutMapping("/status")
    public Result<Boolean> updateStockInStatus(@RequestParam("id") Long id,
                                                @RequestParam("status") Integer status) {
        return Result.success(true);
    }

    /**
     * 删除入库单
     * @param ids
     * @return
     */
    @DeleteMapping()
    public Result<Boolean> deleteStockIn(@RequestParam("ids") List<Long> ids) {
        return Result.success(true);
    }

    /**
     * 获取入库单
     * @param id id
     * @return TableValue
     */
    @GetMapping("/id")
    public Result<TableValue> getStockIn(@RequestParam("id") Long id) {
        return null;
    }

    /**
     * 获取入库单分页
     * @param param
     * @return
     */
    @GetMapping("/page")
    public Result<Page<TableValue>> getStockInPage(@Valid TableValue param) {
        return null;
    }


    /**
     * 导出入库单
     * @param param
     * @param response
     * @throws IOException
     */
    @GetMapping("/export")
    public void exportStockInExcel(@Valid TableValue param,
                                    HttpServletResponse response) throws IOException {
        List<TableValue> list = null;
        // 导出 Excel
        Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams("出库单.xls","数据"), ErpWarehouseRespVO.class, BeanUtils.toBean(list, ErpWarehouseRespVO.class));
        response.setHeader("content-disposition", "attachment;fileName=" + URLEncoder.encode("表101.xls", "UTF-8"));
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        outputStream.close();
        workbook.close();
    }

}