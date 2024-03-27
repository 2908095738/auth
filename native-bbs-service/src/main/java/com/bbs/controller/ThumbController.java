package com.bbs.controller;

import com.bbs.Result;
import com.bbs.cache.ThumbCache;
import com.bbs.dto.param.CreateThumbParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 点赞
 */
@RestController
@RequestMapping("/thumb")
public class ThumbController {


    private ThumbCache cache;

    /**
     * 添加点赞
     * @param param param
     * @return Boolean
     */
    @PutMapping
    public Result<Boolean> createThumb(@RequestBody @Valid CreateThumbParam param){
        //TODO        UserVO currentUser = ThreadLocalUtil.getCurrentUser();
        //保存点赞数据
        param.setUserId(1L);//currentUser.getid
        //更新用户、内容、评论对应点赞数量:redis
        cache.create(param);
        // 计算积分

        //通知对应的用户

        return Result.success();
    }



    @Autowired
    public ThumbController(ThumbCache cache) {
        this.cache = cache;
    }

}
