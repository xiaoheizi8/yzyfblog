package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.Result;
import com.blog.model.entity.Article;
import com.blog.model.entity.ArticleTag;
import com.blog.model.entity.Tag;
import com.blog.model.entity.User;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.MessageMapper;
import com.blog.mapper.TagMapper;
import com.blog.mapper.UserMapper;
import com.blog.model.vo.admin.DashboardOverviewVO;
import com.blog.model.vo.admin.DashboardStatsVO;
import com.blog.model.vo.admin.PieItem;
import com.blog.model.vo.admin.RecentArticleItem;
import com.blog.model.vo.admin.RecentUserItem;
import com.blog.model.vo.admin.RuntimeInfoVO;
import io.swagger.v3.oas.annotations.Operation;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 管理端-仪表盘统计数据。
 *
 * <p>用于首页展示文章数、评论数、留言数等概览信息。
 *
 * @author 一朝风月
 */
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final ArticleMapper articleMapper;
    private final CommentMapper commentMapper;
    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;
    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 应用启动时间，用于计算运行时长。
     */
    private static final Instant START_TIME = Instant.now();

    @Operation(summary = "首页统计概览")
    @GetMapping("/stats")
    public Result<DashboardStatsVO> stats() {
        long articleCount = articleMapper.selectCount(null);
        long commentCount = commentMapper.selectCount(null);
        long messageCount = messageMapper.selectCount(null);
        DashboardStatsVO vo = new DashboardStatsVO(articleCount, commentCount, messageCount);
        return Result.ok(vo);
    }

    /**
     * 仪表盘数据分析（用于饼图、滚动表格）。
     *
     * @return 综合分析数据
     */
    @Operation(summary = "仪表盘数据分析")
    @GetMapping("/overview")
    public Result<DashboardOverviewVO> overview() {
        // 用户状态分布（假定 1 为启用，其他为禁用/冻结）
        long userEnabled = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getStatus, 1));
        long userDisabled = userMapper.selectCount(new LambdaQueryWrapper<User>().ne(User::getStatus, 1));

        // 文章状态分布（0 草稿，1 发布）
        long articleDraft = articleMapper.selectCount(new LambdaQueryWrapper<Article>().eq(Article::getStatus, 0));
        long articlePublished = articleMapper.selectCount(new LambdaQueryWrapper<Article>().eq(Article::getStatus, 1));

        // 标签维度文章数量（取前 10 个）
        java.util.List<Tag> tags = tagMapper.selectList(null);
        java.util.List<PieItem> tagPie = new java.util.ArrayList<>();
        if (tags != null && !tags.isEmpty()) {
            for (Tag tag : tags) {
                long cnt = articleTagMapper.selectCount(
                        new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getTagId, tag.getId())
                );
                if (cnt > 0L) {
                    tagPie.add(new PieItem(tag.getName(), cnt));
                }
            }
            tagPie.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
            if (tagPie.size() > 10) {
                tagPie = tagPie.subList(0, 10);
            }
        }

        // 最近文章列表（用于滚动表格）
        java.util.List<Article> recentArticles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>()
                        .orderByDesc(Article::getPublishTime)
                        .last("limit 20")
        );
        java.util.List<RecentArticleItem> recentArticleItems = new java.util.ArrayList<>();
        if (recentArticles != null) {
            for (Article article : recentArticles) {
                RecentArticleItem item = new RecentArticleItem();
                item.setId(article.getId());
                item.setTitle(article.getTitle());
                item.setViewCount(article.getViewCount() == null ? 0 : article.getViewCount());
                item.setLikeCount(article.getLikeCount() == null ? 0 : article.getLikeCount());
                item.setStatus(article.getStatus());
                item.setPublishTime(article.getPublishTime());
                recentArticleItems.add(item);
            }
        }

        // 最近用户列表（用于滚动表格）
        java.util.List<User> recentUsers = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .orderByDesc(User::getCreateTime)
                        .last("limit 20")
        );
        java.util.List<RecentUserItem> recentUserItems = new java.util.ArrayList<>();
        if (recentUsers != null) {
            for (User user : recentUsers) {
                RecentUserItem item = new RecentUserItem();
                item.setId(user.getId());
                item.setUsername(user.getUsername());
                item.setNickname(user.getNickname());
                item.setStatus(user.getStatus());
                item.setCreateTime(user.getCreateTime());
                recentUserItems.add(item);
            }
        }

        DashboardOverviewVO vo = new DashboardOverviewVO();
        vo.setUserStatus(java.util.Arrays.asList(
                new PieItem("启用用户", userEnabled),
                new PieItem("禁用/冻结用户", userDisabled)
        ));
        vo.setArticleStatus(java.util.Arrays.asList(
                new PieItem("草稿", articleDraft),
                new PieItem("已发布", articlePublished)
        ));
        vo.setArticleTag(tagPie);
        vo.setRecentArticles(recentArticleItems);
        vo.setRecentUsers(recentUserItems);
        vo.setRuntime(buildRuntimeInfo());
        return Result.ok(vo);
    }

    /**
     * 构建运行时系统信息。
     */
    private RuntimeInfoVO buildRuntimeInfo() {
        RuntimeInfoVO info = new RuntimeInfoVO();
        // 启动时间与运行时长
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.of("Asia/Shanghai"));
        info.setStartTime(formatter.format(START_TIME));
        Duration duration = Duration.between(START_TIME, Instant.now());
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("天");
        }
        if (hours > 0) {
            sb.append(hours).append("小时");
        }
        sb.append(minutes).append("分钟");
        info.setUptime(sb.toString());

        // MySQL 状态与版本
        try {
            String version = jdbcTemplate.queryForObject("select version()", String.class);
            info.setMysqlStatus("UP");
            info.setMysqlVersion(version);
        } catch (Exception e) {
            info.setMysqlStatus("DOWN");
            info.setMysqlVersion("-");
        }

        // Redis 状态与版本
        try {
            String pong = stringRedisTemplate.getConnectionFactory().getConnection().ping();
            info.setRedisStatus("PONG".equalsIgnoreCase(pong) ? "UP" : "DOWN");
            Object ver = stringRedisTemplate.getConnectionFactory().getConnection().info("server").get("redis_version");
            info.setRedisVersion(ver != null ? ver.toString() : "-");
        } catch (Exception e) {
            info.setRedisStatus("DOWN");
            info.setRedisVersion("-");
        }
        return info;
    }

}

