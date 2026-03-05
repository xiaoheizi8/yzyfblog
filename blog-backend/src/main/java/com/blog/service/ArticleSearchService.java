package com.blog.service;

import com.blog.common.PageResult;
import com.blog.model.entity.Article;

/**
 * 文章搜索统一接口（MySQL / Elasticsearch 可配置）
 *
 * @author blog
 */
public interface ArticleSearchService {

    /**
     * 关键词搜索，支持高亮（由实现类决定）
     */
    PageResult<Article> search(String keyword, long current, long size);
}
