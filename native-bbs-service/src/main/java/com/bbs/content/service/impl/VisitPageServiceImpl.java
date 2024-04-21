package com.bbs.content.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.content.dto.GetContentDto;
import com.bbs.content.entity.NewTag;
import com.bbs.content.entity.News;
import com.bbs.content.entity.Tag;
import com.bbs.content.entity.VisitPage;
import com.bbs.content.enums.NewCommentStatus;
import com.bbs.content.mapper.VisitPageMapper;
import com.bbs.content.service.VisitPageService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 *
 */
@Service
public class VisitPageServiceImpl extends MPJBaseServiceImpl<VisitPageMapper, VisitPage>
    implements VisitPageService{

    @Override
    public Page<GetContentDto> getListByUserId(Integer current, Integer size, Long currentUserId, String title) {
        return selectJoinListPage(new Page<>(current, size), GetContentDto.class, new MPJLambdaWrapper<VisitPage>()
                .selectAll(VisitPage.class)
                .selectAll(News.class)
                .leftJoin(News.class,News::getNewId,VisitPage::getNewId)
                .selectCollection(Tag.class, GetContentDto::getTags)
                .leftJoin(NewTag.class, NewTag::getNewId, News::getNewId)
                .leftJoin(Tag.class, Tag::getId,NewTag::getTagId)
                .eq(News::getDeleteFlag, 0)
                .eq(News::getStatus, NewCommentStatus.HAVE_RELEASED.getCode())
                .orderBy(true, false, VisitPage::getCreateTime)

                .eq(VisitPage::getUserId,currentUserId)

                .like(StringUtils.isNotBlank(title), News::getTitle, title)
        );
    }

    @Override
    public void createVisitPage(Long currentUserId, Long newId) {
        VisitPage one = lambdaQuery().eq(VisitPage::getUserId, currentUserId).eq(VisitPage::getNewId, newId).one();
        if(Objects.isNull(one)){
            one = new VisitPage();
            one.setUserId(currentUserId);
            one.setNewId(newId);
            save(one);
        }
    }


}




