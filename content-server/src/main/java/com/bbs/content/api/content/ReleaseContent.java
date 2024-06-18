package com.bbs.content.api.content;

import cn.hutool.http.HtmlUtil;
import cn.hutool.json.JSONUtil;
import com.bbs.content.converter.ContentConverter;
import com.bbs.content.entity.Article;
import com.bbs.content.entity.Audit;
import com.bbs.content.entity.Content;
import com.bbs.content.service.ArticleService;
import com.bbs.content.service.AuditService;
import com.bbs.content.service.ContentService;
import com.bbs.content.service.SensitiveWordService;
import com.bbs.content.util.AuthUtil;
import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

import static java.util.Objects.nonNull;

@RestController
@RequestMapping
public class ReleaseContent {

    @Resource
    private ContentConverter converter;
    @Resource
    private ArticleService articleService;
    @Resource
    private ContentService contentService;
    @Resource
    private AuditService auditService;
    @Resource
    private AuthUtil.UserAPI userAPI;
    @Resource
    private TransactionDefinition transactionDefinition;
    @Resource
    private DataSourceTransactionManager transactionManager;
    @Resource
    private SensitiveWordService sensitiveWordService;

    @PutMapping
    public void release(@Valid @RequestBody Param param) throws IllegalArgumentException {
        String content = checkContent(param.getContent());  //校验内容 throw IllegalArgumentException

        Long loginUserID = userAPI.getLoginUser().getId();

        Article entity = converterParam(param, loginUserID);

        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            createArticle(entity);                      //1. 创建文章
            saveArticleContent(content);                //2. 单独保存内容（HTML）
            readyForAudit(param.getNo(), loginUserID);  //3. 准备审核

            transactionManager.commit(transaction);
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new RuntimeException(e);
        }
    }

    private void createArticle(Article entity) {
        articleService.save(entity);
    }

    private void saveArticleContent(String content) {
        contentService.save(new Content(content));
    }

    private void readyForAudit(String contentNO, Long loginUserID) {
        auditService.save(new Audit(contentNO, loginUserID));
    }

    private String checkContent(String content) throws IllegalArgumentException {
        // 匹配敏感关键字
        Preconditions.checkArgument(sensitiveWordService.notMatch(content), "内容包含敏感词汇，请重新编辑");
        // 过滤HTML文本，防止XSS攻击
        return HtmlUtil.filter(content);
    }

    private Article converterParam(Param param, Long loginUserID) {
        Article entity = converter.toEntity(param);
        List<String> cover = param.getCover();
        if(nonNull(cover) && cover.size() > NumberUtils.INTEGER_ZERO) entity.setCover(JSONUtil.toJsonPrettyStr(param.getCover()));
        entity.setCreateId(loginUserID);
        return entity;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {

        /**
         * 内容编号
         */
        @NotNull(message = "内容编号不能为空！")
        private String no;

        /**
         * 标题
         */
        @NotNull(message = "标题不能为空！")
        private String title;

        /**
         * 封面（轮播图）
         */
        private List<String> cover;

        /**
         * 设定发布时间
         */
        private Date scheduledReleaseTime;

        /**
         * 精度
         */
        private String longitude;

        /**
         * 纬度
         */
        private String latitude;

        /**
         * 发布地址
         */
        private String addr;

        /**
         * 内容（html）
         */
        private String content;

        /**
         * 标签
         */
        private List<Tag> tags;
    }

    /**
     * 标签
     */
    public static class Tag {

        private Long id;

        private String tag;
    }
}
