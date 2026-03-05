package com.blog.model.vo.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端-文章分页列表 VO。
 *
 * <p>用于文章管理页展示文章的基础信息以及作者名称等。
 *
 * @author 一朝风月
 */
@Data
public class ArticleAdminVO {

    private Long id;

    private String title;

    private String summary;

    private Integer viewCount;

    private Integer likeCount;

    private Integer commentCount;

    /**
     * 0 草稿，1 发布。
     */
    private Integer status;

    /**
     * 作者名称（优先昵称，其次用户名）。
     */
    private String authorName;

    /**
     * 发布时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Shanghai")
    private LocalDateTime publishTime;
}

