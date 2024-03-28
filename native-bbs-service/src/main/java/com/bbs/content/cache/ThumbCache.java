package com.bbs.content.cache;

import com.bbs.content.dto.param.CreateThumbParam;
import com.bbs.content.dto.param.CancelThumbParam;

import java.util.List;

public interface ThumbCache {

    void create(CreateThumbParam param);

    Integer countBy(Long newId, Long userId, List<Long> commentIds, int type);

    void cancel(CancelThumbParam param);
}
