package com.bbs.financial.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.entity.Certificate;
import com.bbs.financial.service.CertificateService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static com.bbs.Result.success;

/**
 * 记账凭证Controller
 * @author vctgo
 * @date 2024-05-13
 */
@RestController
@RequestMapping("/certificate")
public class CertificateController {

    @Resource
    private CertificateService certificateService;

    /**
     * 查询记账凭证列表
     */
    @GetMapping("/list")
    public Result<Page<Certificate>> list(Certificate certificate, @RequestParam Integer current, @RequestParam Integer size)
    {
        return success(certificateService.page(new Page<>(current, size), new QueryWrapper<>(certificate)));
    }

    /**
     * 获取记账凭证详细信息
     */
    @GetMapping(value = "/{id}")
    public Result<Certificate> getInfo(@PathVariable("id") Long id)
    {
        return success(certificateService.getById(id));
    }

    /**
     * 新增记账凭证
     */
    @PostMapping
    public Result<Boolean> add(@RequestBody Certificate certificate)
    {
        certificateService.save(certificate);
        return success();
    }

    /**
     * 删除记账凭证
     */
    @DeleteMapping("/{ids}")
    public Result<Boolean> remove(@PathVariable List<Long> ids)
    {
        certificateService.getBaseMapper().deleteBatchIds(ids);
        return success();
    }
}
