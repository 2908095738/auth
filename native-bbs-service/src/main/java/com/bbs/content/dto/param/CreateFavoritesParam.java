package com.bbs.content.dto.param;

import lombok.Data;

@Data
public class CreateFavoritesParam {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 内容id
     */
    private Long newId;


}
