package com.bbs.content.dto.param;

import lombok.Data;

@Data
public class DelFavoritesParam {


    /**
     * 用户id
     */
    private Long userId;

    /**
     * 内容id
     */
    private Long newId;

    /**
     * 删除状态：0未删除  1已删除
     */
    private Integer deleteFlag = 1;

}
