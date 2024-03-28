package com.bbs.content.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.content.converter.CommentConverter;
import com.bbs.content.dto.GetUserNewsDto;
import com.bbs.content.dto.param.CreateCommentParam;
import com.bbs.content.service.CommentService;
import com.bbs.content.util.IpConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * 评论
 */
@RestController
@RequestMapping("/comment")
public class CommentController {

    private CommentService service;
    private CommentConverter converter;

    /**
     * 添加评论
     * @param param param
     * @param request request
     * @return Boolean
     */
    @PutMapping
    public Result<Boolean> createComment(@RequestBody @Valid CreateCommentParam param, HttpServletRequest request){
        //TODO        UserVO currentUser = ThreadLocalUtil.getCurrentUser();
        param.setCreateId(1L);//currentUser.getid
        String ip = IpConfig.getIpAdrress(request);//获取ip
        param.setIp(ip);

        service.save(converter.toEntity(param));
        // 计算内容分数

        //通知对应的用户

        return Result.success(true);
    }

    /**
     *根据主键查评论、点赞
     * @param newId 文章id
     * @param current 第几页
     * @param size 几条
     * @return Page<GetUserNewsDto.CommentByNewIdDto>
     */
    @GetMapping
    public Result<Page<GetUserNewsDto.CommentByNewIdDto>> getPageByNewId(@NotNull Long newId, @NotNull Integer current, @NotNull Integer size){
        //获取评论分页列表
        Page<GetUserNewsDto.CommentByNewIdDto> list = service.getPageByNewId(newId, current, size);
        return Result.success(list);
    }



    @Autowired
    public CommentController(CommentService service, CommentConverter converter) {
        this.service = service;
        this.converter = converter;
    }
}
