package com.bbs.cache;

import com.bbs.dto.param.CreateThumbParam;

import java.util.List;

public interface ThumbCache {

    void create(CreateThumbParam param);

    Integer countBy(Long newId, Long userId, List<Long> commentIds, int type);

}
