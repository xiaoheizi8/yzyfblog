package com.blog.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章表
 *
 * @author blog
 */
@Data
@TableName("blog_article")
public class Article extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long categoryId;
    private String title;
    private String summary;
    private String content;       // Markdown 内容
    private String coverImage;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer topFlag;      // 是否置顶
    private Integer status;       // 0草稿 1发布
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")

    private LocalDateTime publishTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")

    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")

    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
