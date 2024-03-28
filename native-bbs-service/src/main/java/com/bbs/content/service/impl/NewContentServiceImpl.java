package com.bbs.content.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.content.mapper.NewContentMapper;
import com.bbs.content.entity.NewContent;
import com.bbs.content.service.NewContentService;
import com.bbs.content.util.SensitiveFilter;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;

/**
 *
 */
@Service
public class NewContentServiceImpl extends ServiceImpl<NewContentMapper, NewContent>
    implements NewContentService{

    public SensitiveFilter sensitiveFilter;

    @Override
    public void createByNew(Long id, String content) {
        //保存文章内容
        NewContent newContent = new NewContent(id);
        newContent.setContent(HtmlUtils.htmlEscape(content));
        newContent.setContent(sensitiveFilter.filter(content));
        save(newContent);
    }






    @Resource
    public void setSensitiveFilter(SensitiveFilter sensitiveFilter) {
        this.sensitiveFilter = sensitiveFilter;
    }

}




