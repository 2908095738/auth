package com.bbs.content.cache;

import com.bbs.content.dto.GetContentDto;
import com.bbs.content.dto.param.CreateNewParam;

import java.util.List;

public interface NewsCache {


    void create(Long newId, CreateNewParam param);

    List<GetContentDto> getHot();
}
