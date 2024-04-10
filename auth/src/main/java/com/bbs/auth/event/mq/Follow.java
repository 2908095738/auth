package com.bbs.auth.event.mq;

import com.bbs.auth.app.follow.DelFollow;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Follow implements Serializable {

    /**
     * 关注者账号id
     */
    private Long userId;

    /**
     * 被关注者账号id
     */
    private Long followUserId;

    /**
     * 创建时间
     */
    private Date createTime;


    public Follow(com.bbs.auth.app.follow.Follow.Param param) {
        this.userId = param.getUserId();
        this.followUserId = param.getFollowUserId();
        this.createTime = new Date();
    }

    public Follow(DelFollow.Param param) {
        this.userId = param.getUserId();
        this.followUserId = param.getFollowUserId();
        this.createTime = new Date();
    }
}
