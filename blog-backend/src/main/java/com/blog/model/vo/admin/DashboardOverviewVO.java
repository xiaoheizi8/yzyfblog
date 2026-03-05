package com.blog.model.vo.admin;

import lombok.Data;

import java.util.List;

/**
 * 仪表盘综合分析 VO。
 *
 * <p>包含用户状态、文章状态、标签文章数量饼图数据以及最近文章、最近用户滚动表格数据。
 *
 * @author 一朝风月
 */
@Data
public class DashboardOverviewVO {

    /**
     * 用户状态分布饼图数据。
     */
    private List<PieItem> userStatus;

    /**
     * 文章状态分布饼图数据。
     */
    private List<PieItem> articleStatus;

    /**
     * 按标签聚合的文章数量饼图数据。
     */
    private List<PieItem> articleTag;

    /**
     * 最近文章列表（用于滚动表格）。
     */
    private List<RecentArticleItem> recentArticles;

    /**
     * 最近用户列表（用于滚动表格）。
     */
    private List<RecentUserItem> recentUsers;

    /**
     * 运行时系统信息（网站运行时间、MySQL/Redis等）。
     */
    private RuntimeInfoVO runtime;
}

