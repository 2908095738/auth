package com.bbs.content.cache;

import com.bbs.content.dto.GetUserNewsDto;
import com.bbs.content.entity.News;

import java.util.List;

public interface NewsCache {


    void create(News news);

    List<GetUserNewsDto> getHot();
}
