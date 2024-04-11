package com.bbs.content.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
//@AllArgsConstructor
@NoArgsConstructor
public class MqAuditStatusDto  implements Serializable {

    private Long newId;

    private Long userId;

//    private String remark;

    private Date createTime;

    public MqAuditStatusDto(Long newId, Long userId, Date createTime) {
        this.newId = newId;
        this.userId = userId;
        this.createTime = createTime;
    }

    private static final long serialVersionUID = 1L;
}
