package com.bbs.chat.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MqFavoritesDto implements Serializable {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 内容id
     */
    private Long newId;

    /**
     * 创建时间
     */
    private Date createTime;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
