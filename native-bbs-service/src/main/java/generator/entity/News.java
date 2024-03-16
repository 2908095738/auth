package generator.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

/**
 * 文章内容
 * @TableName news
 */
@TableName(value ="news")
public class News implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long newsId;

    /**
     * 标题
     */
    private String title;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 内容摘要
     */
    private String summary;

    /**
     * 全部内容
     */
    private String content;

    /**
     * 图片
     */
    private String imageurl;

    /**
     * 视频数
     */
    private Long viewTotal;

    /**
     * 标签id
     */
    private String tagId;

    /**
     * IP
     */
    private String ip;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 最后回复时间
     */
    private Date lastReplyTime;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 状态 10.待审核 20.已发布 110.待审核删除 120.已发布删除
     */
    private Integer status;

    /**
     * 发表时间
     */
    private Date createTime;

    /**
     * 创建id
     */
    private Long createId;

    /**
     * 修改id
     */
    private Long updateId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 精华
     */
    private Integer essence;

    /**
     * 权重 =P表示热度因子(评论数+点赞数+浏览数)；T表示距离发帖的时间（单位为小时）；G表示"重力因子"（gravityth power），即将帖子排名往下拉的力量，默认值为1.8;  权重小于0时表示强制下沉，不再在热门榜上显示;  超出时间限制的热门话题权重在重新计算时设置为0        KaTeX公式表示：Score = \dfrac{P-1}{(T+2)^G}
     */
    private Double weight;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    public Long getNewsId() {
        return newsId;
    }

    /**
     * 主键
     */
    public void setNewsId(Long newsId) {
        this.newsId = newsId;
    }

    /**
     * 标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 标题
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 用户名
     */
    public String getUserName() {
        return userName;
    }

    /**
     * 用户名
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * 内容摘要
     */
    public String getSummary() {
        return summary;
    }

    /**
     * 内容摘要
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * 全部内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 全部内容
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 图片
     */
    public String getImageurl() {
        return imageurl;
    }

    /**
     * 图片
     */
    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    /**
     * 视频数
     */
    public Long getViewTotal() {
        return viewTotal;
    }

    /**
     * 视频数
     */
    public void setViewTotal(Long viewTotal) {
        this.viewTotal = viewTotal;
    }

    /**
     * 标签id
     */
    public String getTagId() {
        return tagId;
    }

    /**
     * 标签id
     */
    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    /**
     * IP
     */
    public String getIp() {
        return ip;
    }

    /**
     * IP
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * 评论数
     */
    public Integer getCommentCount() {
        return commentCount;
    }

    /**
     * 评论数
     */
    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    /**
     * 最后回复时间
     */
    public Date getLastReplyTime() {
        return lastReplyTime;
    }

    /**
     * 最后回复时间
     */
    public void setLastReplyTime(Date lastReplyTime) {
        this.lastReplyTime = lastReplyTime;
    }

    /**
     * 点赞数
     */
    public Integer getLikeCount() {
        return likeCount;
    }

    /**
     * 点赞数
     */
    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    /**
     * 状态 10.待审核 20.已发布 110.待审核删除 120.已发布删除
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 状态 10.待审核 20.已发布 110.待审核删除 120.已发布删除
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 发表时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 发表时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 创建id
     */
    public Long getCreateId() {
        return createId;
    }

    /**
     * 创建id
     */
    public void setCreateId(Long createId) {
        this.createId = createId;
    }

    /**
     * 修改id
     */
    public Long getUpdateId() {
        return updateId;
    }

    /**
     * 修改id
     */
    public void setUpdateId(Long updateId) {
        this.updateId = updateId;
    }

    /**
     * 排序
     */
    public Integer getSort() {
        return sort;
    }

    /**
     * 排序
     */
    public void setSort(Integer sort) {
        this.sort = sort;
    }

    /**
     * 精华
     */
    public Integer getEssence() {
        return essence;
    }

    /**
     * 精华
     */
    public void setEssence(Integer essence) {
        this.essence = essence;
    }

    /**
     * 权重 =P表示热度因子(评论数+点赞数+浏览数)；T表示距离发帖的时间（单位为小时）；G表示"重力因子"（gravityth power），即将帖子排名往下拉的力量，默认值为1.8;  权重小于0时表示强制下沉，不再在热门榜上显示;  超出时间限制的热门话题权重在重新计算时设置为0        KaTeX公式表示：Score = \dfrac{P-1}{(T+2)^G}
     */
    public Double getWeight() {
        return weight;
    }

    /**
     * 权重 =P表示热度因子(评论数+点赞数+浏览数)；T表示距离发帖的时间（单位为小时）；G表示"重力因子"（gravityth power），即将帖子排名往下拉的力量，默认值为1.8;  权重小于0时表示强制下沉，不再在热门榜上显示;  超出时间限制的热门话题权重在重新计算时设置为0        KaTeX公式表示：Score = \dfrac{P-1}{(T+2)^G}
     */
    public void setWeight(Double weight) {
        this.weight = weight;
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
        News other = (News) that;
        return (this.getNewsId() == null ? other.getNewsId() == null : this.getNewsId().equals(other.getNewsId()))
            && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
            && (this.getUserName() == null ? other.getUserName() == null : this.getUserName().equals(other.getUserName()))
            && (this.getSummary() == null ? other.getSummary() == null : this.getSummary().equals(other.getSummary()))
            && (this.getContent() == null ? other.getContent() == null : this.getContent().equals(other.getContent()))
            && (this.getImageurl() == null ? other.getImageurl() == null : this.getImageurl().equals(other.getImageurl()))
            && (this.getViewTotal() == null ? other.getViewTotal() == null : this.getViewTotal().equals(other.getViewTotal()))
            && (this.getTagId() == null ? other.getTagId() == null : this.getTagId().equals(other.getTagId()))
            && (this.getIp() == null ? other.getIp() == null : this.getIp().equals(other.getIp()))
            && (this.getCommentCount() == null ? other.getCommentCount() == null : this.getCommentCount().equals(other.getCommentCount()))
            && (this.getLastReplyTime() == null ? other.getLastReplyTime() == null : this.getLastReplyTime().equals(other.getLastReplyTime()))
            && (this.getLikeCount() == null ? other.getLikeCount() == null : this.getLikeCount().equals(other.getLikeCount()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getCreateId() == null ? other.getCreateId() == null : this.getCreateId().equals(other.getCreateId()))
            && (this.getUpdateId() == null ? other.getUpdateId() == null : this.getUpdateId().equals(other.getUpdateId()))
            && (this.getSort() == null ? other.getSort() == null : this.getSort().equals(other.getSort()))
            && (this.getEssence() == null ? other.getEssence() == null : this.getEssence().equals(other.getEssence()))
            && (this.getWeight() == null ? other.getWeight() == null : this.getWeight().equals(other.getWeight()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getNewsId() == null) ? 0 : getNewsId().hashCode());
        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
        result = prime * result + ((getUserName() == null) ? 0 : getUserName().hashCode());
        result = prime * result + ((getSummary() == null) ? 0 : getSummary().hashCode());
        result = prime * result + ((getContent() == null) ? 0 : getContent().hashCode());
        result = prime * result + ((getImageurl() == null) ? 0 : getImageurl().hashCode());
        result = prime * result + ((getViewTotal() == null) ? 0 : getViewTotal().hashCode());
        result = prime * result + ((getTagId() == null) ? 0 : getTagId().hashCode());
        result = prime * result + ((getIp() == null) ? 0 : getIp().hashCode());
        result = prime * result + ((getCommentCount() == null) ? 0 : getCommentCount().hashCode());
        result = prime * result + ((getLastReplyTime() == null) ? 0 : getLastReplyTime().hashCode());
        result = prime * result + ((getLikeCount() == null) ? 0 : getLikeCount().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getCreateId() == null) ? 0 : getCreateId().hashCode());
        result = prime * result + ((getUpdateId() == null) ? 0 : getUpdateId().hashCode());
        result = prime * result + ((getSort() == null) ? 0 : getSort().hashCode());
        result = prime * result + ((getEssence() == null) ? 0 : getEssence().hashCode());
        result = prime * result + ((getWeight() == null) ? 0 : getWeight().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", newsId=").append(newsId);
        sb.append(", title=").append(title);
        sb.append(", userName=").append(userName);
        sb.append(", summary=").append(summary);
        sb.append(", content=").append(content);
        sb.append(", imageurl=").append(imageurl);
        sb.append(", viewTotal=").append(viewTotal);
        sb.append(", tagId=").append(tagId);
        sb.append(", ip=").append(ip);
        sb.append(", commentCount=").append(commentCount);
        sb.append(", lastReplyTime=").append(lastReplyTime);
        sb.append(", likeCount=").append(likeCount);
        sb.append(", status=").append(status);
        sb.append(", createTime=").append(createTime);
        sb.append(", createId=").append(createId);
        sb.append(", updateId=").append(updateId);
        sb.append(", sort=").append(sort);
        sb.append(", essence=").append(essence);
        sb.append(", weight=").append(weight);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}