package com.blog.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.PageResult;
import com.blog.model.entity.Article;
import com.blog.mapper.ArticleMapper;
import com.blog.service.ArticleSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * MySQL 全文/模糊搜索
 *
 * @author blog
 */
@Service("mysqlArticleSearchService")
@RequiredArgsConstructor
public class MysqlArticleSearchServiceImpl implements ArticleSearchService {

    private final ArticleMapper articleMapper;

    @Override
    public PageResult<Article> search(String keyword, long current, long size) {
        Page<Article> page = new Page<>(current, size);
        IPage<Article> result = articleMapper.searchByKeyword(page, keyword);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }
}
