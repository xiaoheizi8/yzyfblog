package com.blog.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论表（支持表情、回复）
 *
 * @author blog
 */
@Data
@TableName("blog_comment")
public class Comment extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long articleId;
    private Long userId;
    private Long parentId;    // 父评论ID，0为顶级
    private Long replyToId;   // 回复的目标评论ID
    private String content;   // 支持表情、Markdown
    private Integer likeCount;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")

    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")

    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
