package com.bbs.auth.api.vx;

import com.bbs.auth.service.WeiXinService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api(tags = "微信菜单相关接口")
@RestController
@Slf4j
public class WXMenuAPI {


    @Resource
    private WeiXinService weiXinService;

    /**
     * 创建微信菜单
     */
    @PutMapping(value = "/weixin/menu")
    @ApiOperation("创建微信菜单")
    public String createMenu() {
        return weiXinService.createMenu();
    }


    /**
     * 删除微信菜单
     */
    @DeleteMapping(value = "/weixin/menu")
    public String deleteMenu() {
//        return weiXinService.deleteMenu();
        return "";
    }


    /**
     * 获取微信菜单
     */
    @GetMapping(value = "/weixin/menu")
    public String getMenu() {
        return weiXinService.getMenu();
    }

}
