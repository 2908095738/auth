package com.bbs.content.cache;

import com.bbs.content.dto.GetContentDto;
import com.bbs.content.dto.param.CreateNewParam;

import java.util.List;
import java.util.Map;

public interface NewsCache {


    void create(Long newId, CreateNewParam param);

    List<GetContentDto> getHot();

    Integer intrVisit(Long newId);

    Map<Long,Integer> getVisit(List<Long> newIds);
}
