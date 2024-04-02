package com.bbs.content.dto.param;

import com.bbs.vo.BaseParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class QueryNewsParam extends BaseParam {

    /**
     *
     */
    private String title;

    /**
     * 标签ids
     */
    private List<Long> tagIds;


}
