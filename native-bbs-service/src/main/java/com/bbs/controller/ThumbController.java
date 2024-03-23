package com.bbs.controller;

import com.bbs.Result;
import com.bbs.converter.ThumbConverter;
import com.bbs.dto.param.CreateThumbParam;
import com.bbs.service.ThumbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 点赞
 */
@RestController
@RequestMapping("/thumb")
public class ThumbController {

    private ThumbService service;
    private ThumbConverter converter;

    /**
     * 添加点赞
     * @param
     * @return
     */
    @PutMapping
    public Result createThumb(CreateThumbParam param){
        //TODO        UserVO currentUser = ThreadLocalUtil.getCurrentUser();
        param.setUserId(1L);//currentUser.getid
        service.save(converter.toEntity(param));
        // 计算内容分数

        //通知对应的用户

        return Result.success();
    }



    @Autowired
    public ThumbController(ThumbService service, ThumbConverter converter) {
        this.service = service;
        this.converter = converter;
    }

}
