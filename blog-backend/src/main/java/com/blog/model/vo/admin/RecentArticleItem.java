package com.blog.model.vo.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 仪表盘-最近文章条目。
 *
 * @author 一朝风月
 */
@Data
public class RecentArticleItem {

    /**
     * 文章 ID。
     */
    private Long id;

    /**
     * 标题。
     */
    private String title;

    /**
     * 浏览量。
     */
    private Integer viewCount;

    /**
     * 点赞数。
     */
    private Integer likeCount;

    /**
     * 状态：0 草稿，1 发布。
     */
    private Integer status;

    /**
     * 发布时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Shanghai")
    private LocalDateTime publishTime;
}

