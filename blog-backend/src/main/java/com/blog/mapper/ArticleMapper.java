package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.model.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章 Mapper
 *
 * @author blog
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * MySQL 全文搜索（高亮分词需在应用层或 ES 做）
     */
    IPage<Article> searchByKeyword(Page<Article> page, @Param("keyword") String keyword);

    /**
     * 推荐文章（按浏览量、点赞等）
     */
    List<Article> selectRecommend(@Param("excludeId") Long excludeId, @Param("limit") int limit);
}
