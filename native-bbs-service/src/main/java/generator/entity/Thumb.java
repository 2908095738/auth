package generator.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

/**
 * 点赞
 * @TableName thumb
 */
@TableName(value ="thumb")
public class Thumb implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 话题或评论id
     */
    private Integer tcId;

    /**
     * 发布话题或评论的用户id
     */
    private Integer postUserId;

    /**
     * 话题或评论点赞的用户id
     */
    private Integer userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 点赞内容摘要
     */
    private String tcSummary;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    public Integer getId() {
        return id;
    }

    /**
     * id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 话题或评论id
     */
    public Integer getTcId() {
        return tcId;
    }

    /**
     * 话题或评论id
     */
    public void setTcId(Integer tcId) {
        this.tcId = tcId;
    }

    /**
     * 发布话题或评论的用户id
     */
    public Integer getPostUserId() {
        return postUserId;
    }

    /**
     * 发布话题或评论的用户id
     */
    public void setPostUserId(Integer postUserId) {
        this.postUserId = postUserId;
    }

    /**
     * 话题或评论点赞的用户id
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * 话题或评论点赞的用户id
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * 创建时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 修改时间
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 修改时间
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 点赞内容摘要
     */
    public String getTcSummary() {
        return tcSummary;
    }

    /**
     * 点赞内容摘要
     */
    public void setTcSummary(String tcSummary) {
        this.tcSummary = tcSummary;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Thumb other = (Thumb) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getTcId() == null ? other.getTcId() == null : this.getTcId().equals(other.getTcId()))
            && (this.getPostUserId() == null ? other.getPostUserId() == null : this.getPostUserId().equals(other.getPostUserId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getTcSummary() == null ? other.getTcSummary() == null : this.getTcSummary().equals(other.getTcSummary()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTcId() == null) ? 0 : getTcId().hashCode());
        result = prime * result + ((getPostUserId() == null) ? 0 : getPostUserId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getTcSummary() == null) ? 0 : getTcSummary().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", tcId=").append(tcId);
        sb.append(", postUserId=").append(postUserId);
        sb.append(", userId=").append(userId);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", tcSummary=").append(tcSummary);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}