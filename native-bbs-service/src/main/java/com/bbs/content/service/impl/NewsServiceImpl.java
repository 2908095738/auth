package com.bbs.content.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.content.cache.FileCache;
import com.bbs.content.converter.NewsConverter;
import com.bbs.content.dto.GetContentDto;
import com.bbs.content.dto.GetUserNewsDto;
import com.bbs.content.dto.param.CreateNewParam;
import com.bbs.content.dto.param.QueryNewsParam;
import com.bbs.content.entity.Fan;
import com.bbs.content.entity.NewContent;
import com.bbs.content.entity.NewTag;
import com.bbs.content.entity.News;
import com.bbs.content.enums.NewCommentStatus;
import com.bbs.content.mapper.NewsMapper;
import com.bbs.content.service.NewsService;
import com.bbs.content.util.SensitiveFilter;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 *
 */
@Service
public class NewsServiceImpl extends MPJBaseServiceImpl<NewsMapper, News> implements NewsService {

    private NewsConverter converter;

    private SensitiveFilter sensitiveFilter;


    private FileCache fileCache;

    private Map<Integer, GetContentDto> newMap = new HashMap<>();
    private static Random random = new Random();

    private static Integer current = 1;

    /**
     * 返回十条用户没看过的内容
     *
     * @return List<GetUserNewsDto>
     */
    private List<GetContentDto> getRandomNew() {
        Page<GetContentDto> pageByRecommend = getPageByRecommend(current);
        List<GetContentDto> result = new ArrayList<>();
        if (CollUtil.isNotEmpty(pageByRecommend.getRecords())&&pageByRecommend.getRecords().size()<10) {
            return pageByRecommend.getRecords();
        }{
            if (CollUtil.isNotEmpty(pageByRecommend.getRecords())) {
                for (int i = 0; i < pageByRecommend.getRecords().size(); i++) {
                    newMap.put(i, pageByRecommend.getRecords().get(i));
                }
            }
            if (newMap.isEmpty()) {
                current++;
                // 所有文章都已显示过，重置已显示文章集合并重新随机化所有文章
                Page<GetContentDto> getUserNewsDtoPage = getPageByRecommend(current);
                if (CollUtil.isNotEmpty(getUserNewsDtoPage.getRecords()))
                    for (int i = 0; i < getUserNewsDtoPage.getRecords().size(); i++) {
                        newMap.put(i, getUserNewsDtoPage.getRecords().get(i));
                    }
                return result;
            }
            Integer mapSize = newMap.size();
            // 从剩余文章中随机选择一篇
            Integer randomIndex = random.nextInt(mapSize);
            for (int i = 0; i < 10; i++) {
                randomIndex = addResult(randomIndex, result, mapSize);
            }
        }

        return result;
    }


    private Integer addResult(int randomIndex, List<GetContentDto> result, Integer mapSize) {
        GetContentDto getUserNewsDto = newMap.get(randomIndex);
        if (Objects.nonNull(getUserNewsDto)) {
            result.add(getUserNewsDto);
            newMap.remove(randomIndex);
            randomIndex = random.nextInt(mapSize);
            return randomIndex;
        }
        {
            randomIndex = random.nextInt(mapSize);
            return addResult(randomIndex, result, mapSize);
        }
    }


    @Override
    public Long createNewsId(Long createId, String userName) {
        News news = new News().setCreateId(createId).setUpdateId(createId).setUserName(userName).setDeleteFlag(1);
        save(news);
        return news.getNewId();
    }

    @Override
    public void delete(Long newId, Long userId) {
        News news = getOptById(newId).orElseThrow(() -> new RuntimeException("数据不存在"));
        updateById(new News().setNewId(newId).setDeleteFlag(1));
        List<String> filePathList = Arrays.asList(news.getImageUrl().split(","));
        filePathList.addAll(Arrays.asList(news.getViewUrl().split(",")));
        fileCache.delAllFiles(filePathList, newId);
    }

    @Override
    public void updateStatus(Integer status, Long newId) {
        News news = new News();
        news.setNewId(newId);
        news.setStatus(status);
        updateById(news);
    }


    /**
     * 创建文章或视频
     *
     * @param param 文章或视频
     * @return Long
     */
    @Override
    public News createNews(CreateNewParam param) {
        News news = converter.toEntity(param);
        // 转义 HTML 标记，防止在 HTML 标签中注入攻击语句
        news.setTitle(HtmlUtils.htmlEscape(news.getTitle()));
        // 过滤敏感词
        news.setTitle(sensitiveFilter.filter(news.getTitle()))
                .setStatus(NewCommentStatus.WAIT_FOR_REVIEW.getCode())
                .setUpdateId(news.getCreateId())
                .setDeleteFlag(0);
        news.setImageUrl(String.join(",",param.getImageUrlList()));
        news.setViewUrl(String.join(",",param.getViewUrlList()));
        updateById(news);
        return news;
    }

    @Override
    public Page<GetContentDto> getListByQuery(QueryNewsParam param) {
        return selectJoinListPage(new Page<>(param.getCurrent(), param.getSize()), GetContentDto.class, new MPJLambdaWrapper<News>()
                .selectAll(News.class)
                .like(StringUtils.isNotBlank(param.getTitle()), News::getTitle, param.getTitle())
                .in(CollUtil.isNotEmpty(param.getTagIds()), NewTag::getTagId, param.getTagIds())
                .eq(News::getDeleteFlag, 0)
                .orderBy(true, false, News::getCreateTime)
        );
    }

    /**
     * 查询用户主页上发布内容集合
     *
     * @param userId  用户id
     * @param current 第几页
     * @param size    几条
     * @param flag    是否是用户自己  true：是
     * @return GetUserAccountDto.GetUserNewsDto
     */
    @Override
    public Page<GetContentDto> getListByUserId(Long userId, Integer current, Integer size, boolean flag) {
        return selectJoinListPage(new Page<>(current, size), GetContentDto.class, new MPJLambdaWrapper<News>()
                .selectAll(News.class)
//                .selectAssociation(NewContent.class, GetUserNewsDto::getContent, o -> o.result(NewContent::getContent))
//                .leftJoin(NewContent.class, NewContent::getNewId, News::getNewId)
//                .selectCollection(NewTag.class, GetUserNewsDto::getTagIds, o -> o.result(NewTag::getTagId))
//                .leftJoin(NewTag.class, NewTag::getNewId, News::getNewId)
                .orderBy(true, false, News::getCreateTime)
                .eq(News::getCreateId, userId)
                .eq(News::getDeleteFlag, 0)
                .in(flag, News::getStatus, NewCommentStatus.HAVE_RELEASED.getCode(), NewCommentStatus.WAIT_FOR_REVIEW.getCode())
                .eq(!flag, News::getStatus, NewCommentStatus.HAVE_RELEASED.getCode())
        );
    }


    /**
     * 查询推荐页上的内容简要信息
     *
     * @return GetUserNewsDto
     */
    @Override
    public List<GetContentDto> getListByRecommend() {
        return getRandomNew();
    }

    private Page<GetContentDto> getPageByRecommend(Integer current) {
        return selectJoinListPage(new Page<>(current, 50), GetContentDto.class, new MPJLambdaWrapper<News>()
                .selectAll(News.class)
//                .selectAssociation(NewContent.class, GetUserNewsDto::getContent, o -> o.result(NewContent::getContent))
//                .leftJoin(NewContent.class, NewContent::getNewId, News::getNewId)
//                .selectCollection(NewTag.class, GetUserNewsDto::getTagIds, o -> o.result(NewTag::getTagId))
//                .leftJoin(NewTag.class, NewTag::getNewId, News::getNewId)
                .eq(News::getStatus, NewCommentStatus.HAVE_RELEASED.getCode())
                .eq(News::getDeleteFlag, 0)
                .orderBy(true, false, News::getCreateTime)
                .or()
                .orderBy(false, true, News::getCreateTime)
                .or()
                .orderBy(true, false, News::getLastReplyTime)
                .or()
                .orderBy(false, true, News::getLastReplyTime)
                .or()
                .orderBy(true, false, News::getUpdateTime)
                .or()
                .orderBy(false, true, News::getUpdateTime)
        );
    }

    /**
     * 查询关注页上的内容简要信息
     *
     * @param userId  用户id
     * @param current 第几页
     * @param size    几条
     * @return GetUserAccountDto.GetUserNewsDto
     */
    @Override
    public Page<GetContentDto> getListByFollower(Long userId, Integer current, Integer size) {
        return selectJoinListPage(new Page<>(current, size), GetContentDto.class, new MPJLambdaWrapper<News>()
                .selectAll(News.class)
//                .selectAssociation(NewContent.class, GetUserNewsDto::getContent, o -> o.result(NewContent::getContent))
//                .leftJoin(NewContent.class, NewContent::getNewId, News::getNewId)
//                .selectCollection(NewTag.class, GetUserNewsDto::getTagIds, o -> o.result(NewTag::getTagId))
//                .leftJoin(NewTag.class, NewTag::getNewId, News::getNewId)
                .eq(News::getDeleteFlag, 0)
                .eq(News::getStatus, NewCommentStatus.HAVE_RELEASED.getCode())
                .leftJoin(Fan.class, Fan::getUserId, News::getCreateId)
                .eq(News::getCreateId, userId)
                .orderBy(true, false, News::getCreateTime)
        );
    }

    /**
     * 根据主键查全部内容、点赞
     *
     * @param newId 文章id
     * @return GetUserNewsDto
     */
    @Override
    public GetUserNewsDto getOneById(Long newId) {
        return selectJoinOne(GetUserNewsDto.class, new MPJLambdaWrapper<News>()
                .selectAll(News.class)
                .selectCollection(NewTag.class, GetUserNewsDto::getTagIds, o -> o.result(NewTag::getTagId))
                .leftJoin(NewTag.class, NewTag::getNewId, News::getNewId)
                .selectAssociation(NewContent.class, GetUserNewsDto::getContent, o -> o.result(NewContent::getContent))
                .leftJoin(NewContent.class, NewContent::getNewId, News::getNewId)
                .eq(News::getNewId, newId)
                .eq(News::getDeleteFlag, 0)
        );
    }


    @Resource
    public void setConverter(NewsConverter converter) {
        this.converter = converter;
    }

    @Resource
    public void setSensitiveFilter(SensitiveFilter sensitiveFilter) {
        this.sensitiveFilter = sensitiveFilter;
    }


    @Resource
    public void setFileService(FileCache fileCache) {
        this.fileCache = fileCache;
    }
}
