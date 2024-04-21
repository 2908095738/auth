package com.bbs.content.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.content.cache.FileCache;
import com.bbs.content.converter.NewsConverter;
import com.bbs.content.dto.GetContentDto;
import com.bbs.content.dto.GetUserNewsDto;
import com.bbs.content.dto.param.CreateNewParam;
import com.bbs.content.entity.NewContent;
import com.bbs.content.entity.NewTag;
import com.bbs.content.entity.News;
import com.bbs.content.entity.Tag;
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

    private Random random = new Random();

    private Integer dCurrent = 1;
    private Integer dSize = 50;


    public Map<Integer, GetContentDto> getNewMap() {
        if (newMap.isEmpty()) {
            Page<GetContentDto> pageByRecommend = getPageByRecommend();
            if (CollUtil.isNotEmpty(pageByRecommend.getRecords())) {
                dCurrent++;
            }{
                dCurrent--;
                //如果当前页没有数据，获取上一页数据
                pageByRecommend = getPageByRecommend();
            }
            if (CollUtil.isNotEmpty(pageByRecommend.getRecords())) {
                for (int i = 0; i < pageByRecommend.getRecords().size(); i++) {
                    newMap.put(i, pageByRecommend.getRecords().get(i));
                }
            }
        }
        return newMap;
    }

    private Page<GetContentDto> getPageByRecommend() {
        return selectJoinListPage(new Page<>(dCurrent, dSize), GetContentDto.class, new MPJLambdaWrapper<News>()
                .selectAll(News.class)
                .selectCollection(Tag.class, GetContentDto::getTags)
                .leftJoin(NewTag.class, NewTag::getNewId, News::getNewId)
                .leftJoin(Tag.class, Tag::getId,NewTag::getTagId)
                .eq(News::getDeleteFlag, 0)
                .eq(News::getStatus, NewCommentStatus.HAVE_RELEASED.getCode())
                .orderBy(true, false, News::getCreateTime)
        );
    }



    /**
     * 查询推荐页上的内容简要信息
     * @param current 第几页
     * @param size    几条
     * @return GetUserNewsDto
     */
    @Override
    public Page<GetContentDto> getListByRecommend(Integer current, Integer size) {
        Map<Integer, GetContentDto> map = getNewMap();
        if(CollUtil.isNotEmpty(map)){
            return getRandomNew(map,current,size);
        }
        return new Page<>(current, size);
    }

    /**
     * 返回size条用户没看过的内容
     * @param map map
     * @param size 几条
     * @return Page<GetContentDto>
     */
    private Page<GetContentDto> getRandomNew(Map<Integer, GetContentDto> map, Integer current, Integer size) {
        List<GetContentDto> result = new ArrayList<>();
        Integer mapSize = map.size();
        // 从剩余文章中随机选择一篇
        Integer randomIndex = random.nextInt(mapSize);
        for (int i = 0; i < size; i++) {
            randomIndex = addResult(randomIndex,result, mapSize);
        }
        return new Page<GetContentDto>(current, size).setRecords(result);
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



    /**
     * 查询关注页上的内容简要信息
     * @param userIds  关注用户ids
     * @param current 第几页
     * @param size    几条
     * @param title 标题
     * @return GetUserAccountDto.GetUserNewsDto
     */
    @Override
    public Page<GetContentDto> getListByFollower(Integer current, Integer size, List<Long> userIds, String title) {
        return selectJoinListPage(new Page<>(current, size), GetContentDto.class, new MPJLambdaWrapper<News>()
                .selectAll(News.class)
                .selectCollection(Tag.class, GetContentDto::getTags)
                .leftJoin(NewTag.class, NewTag::getNewId, News::getNewId)
                .leftJoin(Tag.class, Tag::getId,NewTag::getTagId)
                .eq(News::getDeleteFlag, 0)
                .eq(News::getStatus, NewCommentStatus.HAVE_RELEASED.getCode())
                .orderBy(true, false, News::getCreateTime)

                .in(News::getCreateId,userIds)

                .like(StringUtils.isNotBlank(title), News::getTitle, title)
        );
    }

    /**
     * 查询本地页上的内容简要信息
     * @param current 第几页
     * @param size    几条
     * @param city 城市信息
     * @param title 标题
     * @return GetUserAccountDto.GetUserNewsDto
     */
    @Override
    public Page<GetContentDto> getListByNative(Integer current, Integer size, String city, String title) {
        return selectJoinListPage(new Page<>(current, size), GetContentDto.class, new MPJLambdaWrapper<News>()
                        .selectAll(News.class)
                        .selectCollection(Tag.class, GetContentDto::getTags)
                        .leftJoin(NewTag.class, NewTag::getNewId, News::getNewId)
                        .leftJoin(Tag.class, Tag::getId,NewTag::getTagId)
                        .eq(News::getDeleteFlag, 0)
                        .eq(News::getStatus, NewCommentStatus.HAVE_RELEASED.getCode())
                        .orderBy(true, false, News::getCreateTime)

                        .like(News::getAddr, city)

                        .like(StringUtils.isNotBlank(title), News::getTitle, title)
//                .in(CollUtil.isNotEmpty(param.getTagIds()), NewTag::getTagId, param.getTagIds())

        );
    }



    /**
     * 查询用户主页上发布内容集合
     * @param userId  用户id
     * @param current 第几页
     * @param size    几条
     * @param flag    是否是用户自己  true：是
     * @param title 标题
     * @return GetUserAccountDto.GetUserNewsDto
     */
    @Override
    public Page<GetContentDto> getListByUserId(Integer current, Integer size, Long userId, boolean flag, String title) {
        return selectJoinListPage(new Page<>(current, size), GetContentDto.class, new MPJLambdaWrapper<News>()
                .selectAll(News.class)
                .selectCollection(Tag.class, GetContentDto::getTags)
                .leftJoin(NewTag.class, NewTag::getNewId, News::getNewId)
                .leftJoin(Tag.class, Tag::getId,NewTag::getTagId)
                .eq(News::getDeleteFlag, 0)
                .eq(News::getStatus, NewCommentStatus.HAVE_RELEASED.getCode())
                .orderBy(true, false, News::getCreateTime)

                .in(flag, News::getStatus, NewCommentStatus.HAVE_RELEASED.getCode(), NewCommentStatus.WAIT_FOR_REVIEW.getCode())
                .eq(!flag, News::getStatus, NewCommentStatus.HAVE_RELEASED.getCode())

                .like(StringUtils.isNotBlank(title), News::getTitle, title)
                //.in(CollUtil.isNotEmpty(param.getTagIds()), NewTag::getTagId, param.getTagIds())

        );
    }


    @Override
    public Long createNewsId(Long createId, String userName) {
        //查询当前用户下是否有审核状态为0，删除状态为一的数据，不存在在添加
        News one = lambdaQuery().eq(News::getCreateId, createId).eq(News::getStatus, 0).eq(News::getDeleteFlag, 1).one();
        if (Objects.isNull(one)) {
            one = new News().setCreateId(createId).setUpdateId(createId).setUserName(userName).setDeleteFlag(1);
            save(one);
        }
        return one.getNewId();
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


    public List<GetContentDto> getHot(){
        return null;
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
                .selectCollection(Tag.class, GetUserNewsDto::getTags)
                .leftJoin(NewTag.class, NewTag::getNewId, News::getNewId)
                .leftJoin(Tag.class, Tag::getId,NewTag::getTagId)
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
