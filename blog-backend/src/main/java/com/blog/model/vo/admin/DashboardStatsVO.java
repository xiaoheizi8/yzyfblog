package com.blog.model.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 仪表盘统计视图对象。
 *
 * <p>用于首页展示文章数、评论数、留言数等概览信息。
 *
 * @author 一朝风月
 */
@Data
@AllArgsConstructor
public class DashboardStatsVO {

    /**
     * 文章数量。
     */
    private long article;

    /**
     * 评论数量。
     */
    private long comment;

    /**
     * 留言数量。
     */
    private long message;
}

