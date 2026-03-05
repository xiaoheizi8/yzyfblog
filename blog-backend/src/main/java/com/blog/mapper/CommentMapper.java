package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.model.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评论 Mapper
 *
 * @author blog
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    List<Comment> selectTreeByArticleId(@Param("articleId") Long articleId);
}
