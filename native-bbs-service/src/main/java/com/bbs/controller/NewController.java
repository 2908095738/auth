package com.bbs.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.dto.GetUserAccountDto;
import com.bbs.dto.GetUserNewsDto;
import com.bbs.dto.param.CreateNewParam;
import com.bbs.service.NewsService;
import com.bbs.util.IpConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 文章/视频
 */
@RestController
@RequestMapping("/new")
public class NewController {

    private NewsService newsService;


    /**
     * 创建文章/视频
     * @param
     * @return
     */
    @PutMapping
    public Result createNews(CreateNewParam param, HttpServletRequest request){
        //TODO        UserVO currentUser = ThreadLocalUtil.getCurrentUser();
        param.setCreateId(1L);//currentUser.getid
        String ip = IpConfig.getIpAdrress(request);//获取ip
        param.setIp(ip);

        newsService.createNews(param);

        // 触发内容事件，通过消息队列将其存入 Elasticsearch 服务器

        // 计算内容分数

        return Result.success();
    }



    /**
     * 查询用户主页上的内容简要信息
     * @param userId 用户id
     * @param current
     * @param size
     * @return
     */
    @GetMapping("/user")
    public Result<Page<GetUserAccountDto.GetUserNewsDto>> getAccountNews(Long userId,Integer current, Integer size){
        Page<GetUserAccountDto.GetUserNewsDto> newsResult = newsService.getListByUserId(userId,current,size);
        return Result.success(newsResult);
    }

    /**
     *根据主键查全部内容、评论、点赞
     * @param newId 文章id
     * @return
     */
    @GetMapping
    public Result<GetUserNewsDto> getOneById(Long newId){
        GetUserNewsDto newsResult = newsService.getOneById(newId);
        return Result.success(newsResult);
    }



    @Autowired
    public NewController(NewsService newsService) {
        this.newsService = newsService;
    }
}
