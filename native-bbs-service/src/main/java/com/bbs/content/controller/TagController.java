package com.bbs.content.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.content.entity.Tag;
import com.bbs.content.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

@Slf4j
@RestController
@RequestMapping("/tag")
public class TagController {

    private TagService service;


    /**
     * 查询标签
     * @param current 第几页
     * @param size 几条
     * @return Page<Tag>
     */
    @GetMapping
    public Result<Page<Tag>> getPage(@NotNull Integer current, @NotNull Integer size){
        Page<Tag> newsResult = service.getAllListPage(current,size);
        return Result.success(newsResult);
    }

    @Autowired
    public TagController(TagService service) {
        this.service = service;
    }
}
