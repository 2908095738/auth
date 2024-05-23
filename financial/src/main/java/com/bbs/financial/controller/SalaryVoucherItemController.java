package com.bbs.financial.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.entity.AuxiliaryCalculation;
import com.bbs.financial.entity.SalaryVoucherItem;
import com.bbs.financial.service.AuxiliaryCalculationService;
import com.bbs.financial.service.SalaryVoucherItemService;
import com.bbs.financial.vo.SalaryVoucherItemVo;
import com.bbs.vo.BaseParam;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static com.bbs.Result.success;

/**
 * 当前公司核算凭证Controller
 * @author vctgo
 * @date 2024-05-15
 */
@RestController
public class SalaryVoucherItemController {

    @Resource
    private SalaryVoucherItemService salaryVoucherItemService;
    @Resource
    private AuxiliaryCalculationService salaryAccountingItemTypeService;

    @Data
    public static class ListParam extends BaseParam {
       Integer type;

       Long cId = 1l;
    }

    /**
     * 查询当前公司核算凭证列表
     */
    @GetMapping("/item/list")
    public Result<Page<SalaryVoucherItemVo>> list(ListParam param)
    {
        return success(salaryVoucherItemService.selectjoinPage(new Page(param.getCurrent(), param.getSize()),param.getCId(),param.getType()));
    }

//    /**
//     * 获取当前公司核算凭证详细信息
//     */
//    @GetMapping(value = "/item/{id}")
//    public Result<SalaryVoucherItem> getInfo(@PathVariable("id") Long id)
//    {
//        return success(salaryVoucherItemService.getById(id));
//    }

    /**
     * 新增当前公司核算凭证
     */
    @PostMapping("/item")
    public Result<Boolean> add(@RequestBody String name)
    {
        AuxiliaryCalculation salaryAccountingItemType = new AuxiliaryCalculation().setName(name);
        salaryAccountingItemTypeService.save(salaryAccountingItemType);
        salaryVoucherItemService.save(new SalaryVoucherItem().setAccountingItemTypeId(salaryAccountingItemType.getId()).setCompanyId(1L).setIsActive(true));
        return success();
    }


    /**
     * 启用/禁用企业核算项目
     */
    @PutMapping("/salary/item/status")
    public Result<Boolean> edit(@RequestParam("salaryVoucherItemId") Long salaryVoucherItemId,@RequestParam("isActive") Boolean isActive)
    {
        salaryVoucherItemService.lambdaUpdate()
                .set(SalaryVoucherItem::getIsActive,isActive)
                .eq(SalaryVoucherItem::getAccountingItemTypeId,salaryVoucherItemId)
                .eq(SalaryVoucherItem::getCompanyId,1l)
                .update();
        return success();
    }




    /**
     * 修改当前公司核算凭证
     */
    @PutMapping("/item")
    public Result<Boolean> edit(@RequestBody SalaryVoucherItem salaryVoucherItem)
    {
        salaryVoucherItemService.updateById(salaryVoucherItem);
        return success();
    }

//    /**
//     * 删除当前公司核算凭证
//     */
//    @DeleteMapping("/item/list/{ids}")
//    public Result<Boolean> remove(@PathVariable List<Long> ids)
//    {
//        salaryVoucherItemService.getBaseMapper().deleteBatchIds(ids);
//        return success();
//    }
}
