package com.bbs.financial.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.api.auth.User;
import com.bbs.api.auth.UserAPI;
import com.bbs.financial.entity.AssetChangeLog;
import com.bbs.financial.service.AssetChangeLogService;
import com.bbs.financial.util.LoginUser;
import com.bbs.vo.BaseParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.bbs.Result.success;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;

/**
 * 资产变动Controller
 * @author vctgo
 * @date 2024-05-29
 */
@RestController
public class AssetChangeLogController {

    @Resource
    private AssetChangeLogService assetChangeLogService;

    @DubboReference
    private UserAPI userAPI;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    private static class Param extends BaseParam {

        private String createTime;
    }



    /**
     * 查询资产变动列表
     */
    @GetMapping("/change/list")
    public Result<Page<AssetChangeLog>> list(Param param)
    {
        Page<AssetChangeLog> page = assetChangeLogService.page(param.toPage(), new QueryWrapper<AssetChangeLog>().lambda()
                .eq(AssetChangeLog::getCompanyId, LoginUser.getCompanyId())
                .like(Objects.nonNull(param.getCreateTime()), AssetChangeLog::getCreateTime, param.getCreateTime())
                .orderBy(true, false, AssetChangeLog::getCreateTime));
        Set<Long> userIds = new HashSet<>();
        page.getRecords().forEach(asset -> userIds.add(asset.getCreateBy()));
        Map<Long, User> userIdMap = null;
        if(userIds.size() > INTEGER_ZERO) {
            userIdMap = userAPI.getUserList(userIds)
                    .stream().collect(Collectors.toMap(User::getId, user -> user));
        }
        boolean userIdMapNoNull = nonNull(userIdMap);
        if(userIdMapNoNull) {
            for (AssetChangeLog asset : page.getRecords()) {
                 asset.setCreateUser(userIdMap.get(asset.getCreateBy()));
            }
        }
        return success(page);
    }

    /**
     * 获取资产变动详细信息
     */
    @GetMapping(value = "/change/{id}")
    public Result<AssetChangeLog> getInfo(@PathVariable("id") Long id)
    {
        return success(assetChangeLogService.getById(id));
    }

    /**
     * 新增资产变动
     */
    @PostMapping("/change")
    public Result<Boolean> add(@RequestBody AssetChangeLog assetChangeLog)
    {
        assetChangeLogService.save(assetChangeLog);
        return success();
    }

    /**
     * 修改资产变动
     */
    @PutMapping("/change")
    public Result<Boolean> edit(@RequestBody AssetChangeLog assetChangeLog)
    {
        assetChangeLogService.updateById(assetChangeLog);
        return success();
    }

    /**
     * 删除资产变动
     */
    @DeleteMapping("/change/{ids}")
    public Result<Boolean> remove(@PathVariable List<Long> ids)
    {
        assetChangeLogService.getBaseMapper().deleteBatchIds(ids);
        return success();
    }
}
