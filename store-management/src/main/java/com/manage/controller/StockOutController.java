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
import org.apache.poi.ss.usermodel.Workbook;
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
 * 管理后台 - 库存出库单
 */
@RestController
@RequestMapping("/stock/out")
public class StockOutController {

    @Resource
    private CustomizeTableService customizeTableService;
    @Resource
    private TableValueService tableValueService;


    /**
     * 创建出库单
     * @param param
     * @return Long
     */
    @PostMapping()
    public Result<Long> createStockOut(@Valid @RequestBody TableValue param) {
        return Result.success(null);
    }

    /**
     * 更新出库单
     * @param param
     * @return Long
     */
    @PutMapping()
    public Result<Boolean> updateStockOut(@Valid @RequestBody TableValue param) {
        return Result.success(true);
    }

    /**
     * 更新出库单的状态
     * @param id
     * @param status
     * @return Boolean
     */
    @PutMapping("/status")
    public Result<Boolean> updateStockOutStatus(@RequestParam("id") Long id,
                                                     @RequestParam("status") Integer status) {
        return Result.success(true);
    }

    /**
     * 删除出库单
     * @param ids
     * @return
     */
    @DeleteMapping()
    public Result<Boolean> deleteStockOut(@RequestParam("ids") List<Long> ids) {
        return Result.success(true);
    }

    /**
     * 获取出库单
     * @param id id
     * @return TableValue
     */
    @GetMapping("/id")
    public Result<TableValue> getStockOut(@RequestParam("id") Long id) {
        return null;
    }

    /**
     * 获取出库单分页
     * @param param
     * @return
     */
    @GetMapping("/page")
    public Result<Page<TableValue>> getStockOutPage(@Valid TableValue param) {
        return null;
    }

    /**
     * 导出出库单 Excel
     * @throws IOException
     */
    @GetMapping("/export")
    public void exportStockOutExcel(@Valid TableValue param,
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