package com.blog.controller.portal;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.model.entity.Article;
import com.blog.model.entity.ArticleTag;
import com.blog.model.entity.Tag;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.TagMapper;
import com.blog.service.ArticleSearchStrategyService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 门户-文章（列表、详情、搜索、推荐、发布）
 *
 * @author blog
 */
@RestController
@RequestMapping("/portal/article")
@RequiredArgsConstructor
public class PortalArticleController {

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final TagMapper tagMapper;
    private final ArticleSearchStrategyService articleSearchStrategyService;

    @Operation(summary = "发布文章")
    @PostMapping
    public Result<Article> publish(@RequestBody Map<String, Object> body) {
        Long userId = StpUtil.getLoginIdAsLong();
        Object titleObj = body.get("title");
        Object contentObj = body.get("content");
        if (titleObj == null || titleObj.toString().isBlank() || contentObj == null || contentObj.toString().isBlank()) {
            return Result.fail("标题或内容不能为空");
        }
        Article a = new Article();
        a.setUserId(userId);
        a.setTitle(titleObj.toString());
        a.setSummary(body.get("summary") == null ? null : body.get("summary").toString());
        if (body.get("categoryId") != null) {
            a.setCategoryId(Long.valueOf(body.get("categoryId").toString()));
        }
        a.setContent(contentObj.toString());
        a.setCoverImage(body.get("coverImage") == null ? null : body.get("coverImage").toString());
        a.setStatus(1);
        a.setPublishTime(LocalDateTime.now());
        a.setCreateTime(LocalDateTime.now());
        a.setUpdateTime(LocalDateTime.now());
        articleMapper.insert(a);

        // 标签处理：支持已有标签ID与自定义标签名
        Object tagIdsObj = body.get("tagIds");
        Object newTagsObj = body.get("newTags");
        java.util.List<Long> tagIds = new java.util.ArrayList<>();
        if (tagIdsObj instanceof java.util.List<?>) {
            for (Object o : (java.util.List<?>) tagIdsObj) {
                if (o != null) {
                    tagIds.add(Long.valueOf(o.toString()));
                }
            }
        }
        java.util.List<String> newTagNames = new java.util.ArrayList<>();
        if (newTagsObj instanceof java.util.List<?>) {
            for (Object o : (java.util.List<?>) newTagsObj) {
                if (o != null && !o.toString().isBlank()) {
                    newTagNames.add(o.toString().trim());
                }
            }
        } else if (newTagsObj instanceof String s) {
            if (!s.isBlank()) {
                for (String part : s.split(",")) {
                    if (!part.isBlank()) {
                        newTagNames.add(part.trim());
                    }
                }
            }
        }

        // 为自定义标签创建 Tag，并记录其ID
        if (!newTagNames.isEmpty()) {
            for (String name : newTagNames) {
                Tag exist = tagMapper.selectOne(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Tag>()
                                .eq(Tag::getName, name)
                                .last("limit 1"));
                if (exist != null) {
                    tagIds.add(exist.getId());
                    continue;
                }
                Tag t = new Tag();
                t.setName(name);
                t.setSlug(name);
                t.setCreateTime(LocalDateTime.now());
                tagMapper.insert(t);
                tagIds.add(t.getId());
            }
        }

        // 写入文章-标签关联
        if (!tagIds.isEmpty()) {
            for (Long tagId : tagIds) {
                if (tagId == null) continue;
                ArticleTag at = new ArticleTag();
                at.setArticleId(a.getId());
                at.setTagId(tagId);
                articleTagMapper.insert(at);
            }
        }

        return Result.ok(a);
    }

    @Operation(summary = "当前用户文章分页（我的文章）")
    @GetMapping("/my")
    public Result<PageResult<Article>> my(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        if (!StpUtil.isLogin()) {
            return Result.fail("请先登录");
        }
        Long userId = StpUtil.getLoginIdAsLong();
        Page<Article> page = new Page<>(current, size);
        LambdaQueryWrapper<Article> q = new LambdaQueryWrapper<Article>()
                .eq(Article::getUserId, userId)
                .orderByDesc(Article::getPublishTime)
                .orderByDesc(Article::getCreateTime);
        Page<Article> result = articleMapper.selectPage(page, q);
        return Result.ok(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    @Operation(summary = "分页列表")
    @GetMapping("/list")
    public Result<PageResult<Article>> list(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId) {
        Page<Article> page = new Page<>(current, size);
        LambdaQueryWrapper<Article> q = new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, 1)
                .orderByDesc(Article::getTopFlag)
                .orderByDesc(Article::getPublishTime);
        if (categoryId != null) {
            q.eq(Article::getCategoryId, categoryId);
        }
        // 按标签筛选：先查出该标签下的文章ID
        if (tagId != null) {
            java.util.List<ArticleTag> ats = articleTagMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ArticleTag>()
                            .eq(ArticleTag::getTagId, tagId));
            if (ats == null || ats.isEmpty()) {
                return Result.ok(PageResult.of(0, current, size, java.util.List.of()));
            }
            java.util.Set<Long> articleIds = new java.util.HashSet<>();
            for (ArticleTag at : ats) {
                if (at.getArticleId() != null) {
                    articleIds.add(at.getArticleId());
                }
            }
            if (articleIds.isEmpty()) {
                return Result.ok(PageResult.of(0, current, size, java.util.List.of()));
            }
            q.in(Article::getId, articleIds);
        }
        Page<Article> result = articleMapper.selectPage(page, q);
        return Result.ok(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    @Operation(summary = "文章详情")
    @GetMapping("/{id}")
    public Result<Article> detail(@PathVariable Long id) {
        Article a = articleMapper.selectById(id);
        if (a == null) {
            return Result.fail("文章不存在");
        }
        UpdateWrapper<Article> uw = new UpdateWrapper<>();
        uw.eq("id", id).setSql("view_count = view_count + 1");
        articleMapper.update(null, uw);
        a.setViewCount(a.getViewCount() == null ? 1 : a.getViewCount() + 1);
        return Result.ok(a);
    }

    @Operation(summary = "搜索（高亮分词由前端或 ES 实现）")
    @GetMapping("/search")
    public Result<PageResult<Article>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        PageResult<Article> data = articleSearchStrategyService.search(keyword, current, size);
        return Result.ok(data);
    }

    @Operation(summary = "推荐文章")
    @GetMapping("/recommend")
    public Result<List<Article>> recommend(
            @RequestParam(required = false) Long excludeId,
            @RequestParam(defaultValue = "5") int limit) {
        List<Article> list = articleMapper.selectRecommend(excludeId, limit);
        return Result.ok(list);
    }
}
