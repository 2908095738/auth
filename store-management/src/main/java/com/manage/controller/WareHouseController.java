package com.manage.controller;


import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.enums.CommonStatusEnum;
import com.bbs.util.BeanUtils;
import com.manage.dto.warehouse.ErpWarehousePageReqVO;
import com.manage.dto.warehouse.ErpWarehouseRespVO;
import com.manage.dto.warehouse.ErpWarehouseSaveReqVO;
import com.manage.entity.WareHouse;
import com.manage.service.WareHouseService;
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

import static com.bbs.util.CollectionUtils.convertList;




/**
 * 管理后台 - 仓库
 */
@RestController
@RequestMapping("/warehouse")
public class WareHouseController {

    @Resource
    private WareHouseService wareHouseService;

    /**
     * 创建仓库
     * @param createReqVO
     * @return
     */
    @PostMapping()
    public Result<Long> createWarehouse(@Valid @RequestBody ErpWarehouseSaveReqVO createReqVO) {
        return Result.success(wareHouseService.createWarehouse(createReqVO));
    }

    /**
     * 更新仓库
     * @param updateReqVO
     * @return
     */
    @PutMapping()
    public Result<Boolean> updateWarehouse(@Valid @RequestBody ErpWarehouseSaveReqVO updateReqVO) {
        wareHouseService.updateWarehouse(updateReqVO);
        return Result.success(true);
    }

    /**
     * 更新仓库默认状态
     * @param id
     * @param defaultStatus
     * @return
     */
    @PutMapping("/default-status")
    public Result<Boolean> updateWarehouseDefaultStatus(@RequestParam("id") Long id,
                                                              @RequestParam("defaultStatus") Integer defaultStatus) {
        wareHouseService.updateWarehouseDefaultStatus(id, defaultStatus);
        return Result.success(true);
    }

    /**
     * 删除仓库
     * @param id
     * @return
     */
    @DeleteMapping()
    public Result<Boolean> deleteWarehouse(@RequestParam("id") Long id) {
        wareHouseService.deleteWarehouse(id);
        return Result.success(true);
    }

    /**
     * 查询仓库
     * @param id
     * @return
     */
    @GetMapping("/id")
    public Result<ErpWarehouseRespVO> getWarehouse(@RequestParam("id") Long id) {
        WareHouse warehouse = wareHouseService.getWarehouse(id);
        return Result.success(BeanUtils.toBean(warehouse, ErpWarehouseRespVO.class));
    }

    /**
     * 查询仓库分页
     * @param pageReqVO
     * @return
     */
    @GetMapping("/page")
    public Result<Page<ErpWarehouseRespVO>> getWarehousePage(ErpWarehousePageReqVO pageReqVO) {
        Page<WareHouse> pageResult = wareHouseService.getWarehousePage(pageReqVO);
        return Result.success(BeanUtils.toBean(pageResult, ErpWarehouseRespVO.class));
    }

    /**
     * 查询仓库精简列表 只包含被开启的仓库，主要用于前端的下拉选项
     * @return
     */
    @GetMapping("/simple")
    public Result<List<ErpWarehouseRespVO>> getWarehouseSimpleList() {
        List<WareHouse> list = wareHouseService.getWarehouseListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return Result.success(convertList(list, warehouse -> new ErpWarehouseRespVO().setId(warehouse.getId())
                .setName(warehouse.getName()).setDefaultStatus(warehouse.getDefaultStatus())));
    }

    /**
     * 导出仓库 Excel
     * @param pageReqVO
     * @param response
     */
    @GetMapping("/export")
    public void exportWarehouseExcel(@Valid ErpWarehousePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        List<WareHouse> list = wareHouseService.getWarehousePage(pageReqVO).getRecords();
        // 导出 Excel
        Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams("仓库.xls","数据"), ErpWarehouseRespVO.class, BeanUtils.toBean(list, ErpWarehouseRespVO.class));
        response.setHeader("content-disposition", "attachment;fileName=" + URLEncoder.encode("表101.xls", "UTF-8"));
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        outputStream.close();
        workbook.close();
    }

}