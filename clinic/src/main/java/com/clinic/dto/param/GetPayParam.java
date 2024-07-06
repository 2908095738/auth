package com.clinic.dto.param;

import com.bbs.vo.BaseParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class GetPayParam extends BaseParam {

    /**
     * 姓名
     */
    private String name;

    /**
     * 手机号
     */
    private Long phone;

    /**
     * 状态
     */
    private Integer state;

}
