package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.model.entity.ArticleTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章-标签关联 Mapper。
 *
 * <p>用于在仪表盘等场景下按标签统计文章数量等。
 *
 * @author 一朝风月
 */
@Mapper
public interface ArticleTagMapper extends BaseMapper<ArticleTag> {
}

