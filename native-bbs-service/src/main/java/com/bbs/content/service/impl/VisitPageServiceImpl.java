package com.bbs.content.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.content.dto.VisitPageDto;
import com.bbs.content.entity.News;
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
    public Page<VisitPageDto> getListByUserId(Integer current, Integer size, Long currentUserId, String title) {
        return selectJoinListPage(new Page<>(current, size), VisitPageDto.class, new MPJLambdaWrapper<VisitPage>()
                .selectAll(VisitPage.class)

                .eq(VisitPage::getUserId,currentUserId)
                .leftJoin(News.class,News::getNewId,VisitPage::getNewId)
                .eq(News::getDeleteFlag, 0)
                .eq(News::getStatus, NewCommentStatus.HAVE_RELEASED.getCode())

                .orderBy(true, false, VisitPage::getCreateTime)

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




