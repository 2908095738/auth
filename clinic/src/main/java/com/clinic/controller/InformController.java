package com.clinic.controller;

import cn.hutool.core.collection.CollUtil;
import com.bbs.Result;
import com.clinic.cache.inform.InformCache;
import com.clinic.entity.Inform;
import com.clinic.util.LoginUser;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class InformController {


    @Resource
    private InformCache InformCache;

    /**
     * 查询当前用户未读通知
     * @return Result<List<Inform>>
     */
    @GetMapping("/inform")
    public Result<List<Inform>> getLoginInform() {
        List<Inform> informList = InformCache.getInformList(LoginUser.getId());
        if(CollUtil.isNotEmpty(informList)){
            InformCache.updateUserReadInform(LoginUser.getId());
        }
        return Result.success(informList);
    }

    /**
     * 查询所有通知
     * @return Result<List<Inform>>
     */
    @GetMapping("/back/inform")
    public Result<List<Inform>> getAllInform() {
        List<Inform> informList = InformCache.getInformList(null);
        return Result.success(informList);
    }

    @Data
    public static class InformVo{
        private String title;
        private String content;
        private int type;
    }

    /**
     * 添加通知
     * @param informVo
     * @return
     */
    @PutMapping("/back/inform")
    public Result<Boolean> putInform(@RequestBody InformVo informVo) {
        InformCache.putInform(new Inform(informVo));
        return Result.success();
    }

}
