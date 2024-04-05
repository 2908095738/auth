package com.bbs.content.dto.param;

import com.bbs.vo.BaseParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GetFavoritesParam extends BaseParam {

    /**
     * 标题
     */
    private String title;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户id
     */
    private Long userId;

}
