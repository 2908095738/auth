package generator.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * 通知
 * @TableName notification
 */
@TableName(value ="notification")
public class Notification implements Serializable {
    /**
     * 
     */
    private Long id;

    /**
     * 
     */
    private Long notifier;

    /**
     * 
     */
    private Long receiver;

    /**
     * 
     */
    private Long outerid;

    /**
     * 1回问，2回评，3收藏，4点赞
     */
    private Integer type;

    /**
     * 
     */
    private Long gmtCreate;

    /**
     * 0未读，1已读
     */
    private Integer status;

    /**
     * 
     */
    private String notifierName;

    /**
     * 
     */
    private String outerTitle;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public Long getId() {
        return id;
    }

    /**
     * 
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 
     */
    public Long getNotifier() {
        return notifier;
    }

    /**
     * 
     */
    public void setNotifier(Long notifier) {
        this.notifier = notifier;
    }

    /**
     * 
     */
    public Long getReceiver() {
        return receiver;
    }

    /**
     * 
     */
    public void setReceiver(Long receiver) {
        this.receiver = receiver;
    }

    /**
     * 
     */
    public Long getOuterid() {
        return outerid;
    }

    /**
     * 
     */
    public void setOuterid(Long outerid) {
        this.outerid = outerid;
    }

    /**
     * 1回问，2回评，3收藏，4点赞
     */
    public Integer getType() {
        return type;
    }

    /**
     * 1回问，2回评，3收藏，4点赞
     */
    public void setType(Integer type) {
        this.type = type;
    }

    /**
     * 
     */
    public Long getGmtCreate() {
        return gmtCreate;
    }

    /**
     * 
     */
    public void setGmtCreate(Long gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    /**
     * 0未读，1已读
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 0未读，1已读
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 
     */
    public String getNotifierName() {
        return notifierName;
    }

    /**
     * 
     */
    public void setNotifierName(String notifierName) {
        this.notifierName = notifierName;
    }

    /**
     * 
     */
    public String getOuterTitle() {
        return outerTitle;
    }

    /**
     * 
     */
    public void setOuterTitle(String outerTitle) {
        this.outerTitle = outerTitle;
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
        Notification other = (Notification) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getNotifier() == null ? other.getNotifier() == null : this.getNotifier().equals(other.getNotifier()))
            && (this.getReceiver() == null ? other.getReceiver() == null : this.getReceiver().equals(other.getReceiver()))
            && (this.getOuterid() == null ? other.getOuterid() == null : this.getOuterid().equals(other.getOuterid()))
            && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
            && (this.getGmtCreate() == null ? other.getGmtCreate() == null : this.getGmtCreate().equals(other.getGmtCreate()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getNotifierName() == null ? other.getNotifierName() == null : this.getNotifierName().equals(other.getNotifierName()))
            && (this.getOuterTitle() == null ? other.getOuterTitle() == null : this.getOuterTitle().equals(other.getOuterTitle()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getNotifier() == null) ? 0 : getNotifier().hashCode());
        result = prime * result + ((getReceiver() == null) ? 0 : getReceiver().hashCode());
        result = prime * result + ((getOuterid() == null) ? 0 : getOuterid().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getGmtCreate() == null) ? 0 : getGmtCreate().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getNotifierName() == null) ? 0 : getNotifierName().hashCode());
        result = prime * result + ((getOuterTitle() == null) ? 0 : getOuterTitle().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", notifier=").append(notifier);
        sb.append(", receiver=").append(receiver);
        sb.append(", outerid=").append(outerid);
        sb.append(", type=").append(type);
        sb.append(", gmtCreate=").append(gmtCreate);
        sb.append(", status=").append(status);
        sb.append(", notifierName=").append(notifierName);
        sb.append(", outerTitle=").append(outerTitle);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}