package com.blog.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 文章-标签关联
 *
 * @author blog
 */
@Data
@TableName("blog_article_tag")
public class ArticleTag extends BaseEntity {

    @TableId
    private Long id;
    private Long articleId;
    private Long tagId;
}
