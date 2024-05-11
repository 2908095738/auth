package com.bbs.content.cache;

import com.bbs.content.entity.News;

public interface ContentCache {

    String key(Long newID);

    News get(Long newID);
}
