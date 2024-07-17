package com.bbs.financial.api.asset;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.bbs.api.auth.User;
import com.bbs.api.auth.UserAPI;
import com.bbs.api.auth.company.CompanyAPI;
import com.bbs.financial.entity.*;
import com.bbs.financial.service.AssetService;
import com.bbs.financial.util.LoginUser;
import com.bbs.vo.CompanyStructure;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.*;

@RestController
@RequestMapping
public class ExportAsset {

    @Resource
    private AssetService db;
    @Resource
    private HttpServletResponse response;
    @DubboReference
    private UserAPI userAPI;
    @DubboReference
    private CompanyAPI companyAPI;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {

        private Long startDate;

        private Long endDate;
    }

    @GetMapping("/asset/export")
    public void export(
            
            Param param
    ) {
        Date startDate = null;
        if(nonNull(param.startDate)) {
            startDate = new Date(param.startDate);
        }

        Date endDate = null;
        if(nonNull(param.endDate)) {
            endDate = new Date(param.endDate);
        }

        Date finalStartDate = startDate;
        Date finalEndDate = endDate;
        List<Asset> assets = db.listDeep(new MPJLambdaWrapper<Asset>()
                .selectAll(Asset.class)
                .eq(Asset::getAccountingSetId, LoginUser.getLoginSetId())
                .or(nonNull(startDate), wrapper -> wrapper
                        .ge(Asset::getCreateTime, nonNull(finalStartDate) ? DateUtil.beginOfMonth(finalStartDate) : null)
                        // 最大时间使用传入的 endDate 取当月最后一天（如果只查单月，则 endDate 可空，并使用传入的 startDate 替换计算最后一天）
                        .lt(Asset::getCreateTime, nonNull(finalEndDate) ? DateUtil.offsetMonth(finalEndDate, INTEGER_ONE) : DateUtil.offsetMonth(finalStartDate, INTEGER_ONE))
                )
        );

        if(nonNull(assets) && assets.size() > INTEGER_ZERO) {
            Set<Long> userIds = new HashSet<>();
            Set<Long> structureIds = new HashSet<>();
            assets.forEach(asset -> {
                if(nonNull(asset.getStartDate())) asset.setStartDateStr(DateUtil.formatDateTime(asset.getStartDate()));
                if(nonNull(asset.getNumUnit())) asset.setNumUnitName(asset.getNumUnit().getName());
                if(nonNull(asset.getFixedAssetsAccount())) asset.setFixedAssetsAccountName(asset.getFixedAssetsAccount().getName());
                if(nonNull(asset.getPurchaseAssetsOtherPartAccount())) asset.setPurchaseAssetsOtherPartAccountName(asset.getPurchaseAssetsOtherPartAccount().getName());
                if(nonNull(asset.getTaxesAccount())) asset.setTaxesAccountName(asset.getTaxesAccount().getName());
                if(nonNull(asset.getDepreciationAccount())) asset.setDepreciationAccountName(asset.getDepreciationAccount().getName());
                if(nonNull(asset.getAssetsCleanAccount())) asset.setAssetsCleanAccountName(asset.getAssetsCleanAccount().getName());
                if(nonNull(asset.getImpairmentAccount())) asset.setImpairmentAccountName(asset.getImpairmentAccount().getName());
                if(nonNull(asset.getImpairmentOtherPartAccount())) asset.setImpairmentOtherPartAccountName(asset.getImpairmentOtherPartAccount().getName());



                if(nonNull(asset.getAssetsCertificate())) {
                    Certificate certificate = asset.getAssetsCertificate();
                    asset.setAssetsCertificateName("记-" + certificate.getNo());
                }
                if(nonNull(asset.getAssetsCleanCertificate())) {
                    Certificate certificate = asset.getAssetsCleanCertificate();
                    asset.setAssetsCleanCertificateName("记-" + certificate.getNo());
                }
                if(nonNull(asset.getImpairmentCertificate())) {
                    Certificate certificate = asset.getImpairmentCertificate();
                    asset.setImpairmentCertificateName("记-" + certificate.getNo());
                }
                if(nonNull(asset.getOtherCertificate())) {
                    Certificate certificate = asset.getOtherCertificate();
                    asset.setOtherCertificateName("记-" + certificate.getNo());
                }

                asset.setStatusName(Objects.equals(asset.getStatus(), INTEGER_ZERO) ? "正常" : "清理");
                if(nonNull(asset.getUseUserId())) userIds.add(asset.getUseUserId());
                if(nonNull(asset.getCreateBy())) userIds.add(asset.getCreateBy());
                if(nonNull(asset.getCreateTime())) asset.setCreateTimeStr(DateUtil.formatDateTime(asset.getCreateTime()));
                if(nonNull(asset.getUpdateBy())) userIds.add(asset.getUpdateBy());
                if(nonNull(asset.getUpdateTime())) asset.setUpdateTimeStr(DateUtil.formatDateTime(asset.getUpdateTime()));
                if(nonNull(asset.getStructureId()) && !Objects.equals(asset.getStructureId(), LONG_ZERO)) structureIds.add(asset.getStructureId());
                if(nonNull(asset.getDepreciationMethod())) {
                    if(asset.getDepreciationMethod().equals(INTEGER_ZERO)) {
                        asset.setDepreciationMethodName("平均年限法");
                    } else if(asset.getDepreciationMethod().equals(INTEGER_ONE)) {
                        asset.setDepreciationMethodName("双倍余额递减法");
                    } else {
                        asset.setDepreciationMethodName("不折旧");
                    }
                }
            });
            Map<Long, CompanyStructure> structureMap = null;
            if(structureIds.size() > INTEGER_ZERO) {
                structureMap = companyAPI.searchIdMap(structureIds);
            }
            Map<Long, User> userIdMap = null;
            if(userIds.size() > INTEGER_ZERO) {
                userIdMap = userAPI.getUserIdMap(userIds);
            }
            for (Asset asset : assets) {
                if(nonNull(structureMap) && nonNull(asset.getStructureId())) asset.setStructureName(structureMap.get(asset.getStructureId()).getName());

                if(nonNull(userIdMap)) {
                    if(nonNull(asset.getUseUserId())) asset.setUseUserName(userIdMap.get(asset.getUseUserId()).getName());
                    if(nonNull(asset.getCreateBy())) asset.setCreateUserName(userIdMap.get(asset.getCreateBy()).getName());
                    if(nonNull(asset.getUpdateBy())) asset.setUpdateUserName(userIdMap.get(asset.getUpdateBy()).getName());
                }
            }
        }

        OutputStream out = null;
        ExcelWriter writer = ExcelUtil.getWriter();
        try {
            //一次性写出内容
            writer.write(assets, true);

            out = response.getOutputStream();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
            response.setHeader("content-disposition", "attachment;fileName=asset.xlsx");
            response.setHeader("filename", "asset.xlsx");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            writer.flush(out, true);
            writer.close();
            IoUtil.close(out);
        }
    }
}
