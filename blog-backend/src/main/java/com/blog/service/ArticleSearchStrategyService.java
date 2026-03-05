package com.blog.service;

import com.blog.common.PageResult;
import com.blog.model.entity.Article;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * 文章搜索策略。
 *
 * <p>当前版本统一使用 MySQL 搜索，后续若需要可再接入 Elasticsearch。
 *
 * @author 一朝风月
 */
@Service
public class ArticleSearchStrategyService {

    private final ArticleSearchService mysqlArticleSearchService;

    public ArticleSearchStrategyService(@Qualifier("mysqlArticleSearchService") ArticleSearchService mysqlArticleSearchService) {
        this.mysqlArticleSearchService = mysqlArticleSearchService;
    }

    public PageResult<Article> search(String keyword, long current, long size) {
        // 统一走 MySQL 搜索实现
        return mysqlArticleSearchService.search(keyword, current, size);
    }
}
