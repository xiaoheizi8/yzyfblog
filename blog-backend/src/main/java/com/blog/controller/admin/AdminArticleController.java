package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.annotation.Idempotent;
import com.blog.annotation.Log;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.model.entity.Article;
import com.blog.model.entity.ArticleTag;
import com.blog.model.entity.Tag;
import com.blog.model.entity.User;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.TagMapper;
import com.blog.mapper.UserMapper;
import com.blog.model.vo.admin.ArticleAdminVO;
import com.blog.model.vo.portal.ArticleLikeRankVO;
import com.blog.service.ArticleLikeService;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端-文章（含点赞排行榜）
 *
 * @author blog
 */
@RestController
@RequestMapping("/admin/article")
@RequiredArgsConstructor
public class AdminArticleController {

    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final ArticleTagMapper articleTagMapper;
    private final TagMapper tagMapper;
    private final ArticleLikeService articleLikeService;

    @Operation(summary = "点赞排行榜")
    @GetMapping("/ranking")
    public Result<List<ArticleLikeRankVO>> ranking(@RequestParam(defaultValue = "20") int limit) {
        return Result.ok(articleLikeService.getRanking(limit));
    }

    @Operation(summary = "文章分页列表")
    @GetMapping("/page")
    public Result<PageResult<ArticleAdminVO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Integer status) {
        Page<Article> page = new Page<>(current, size);
        LambdaQueryWrapper<Article> q = new LambdaQueryWrapper<Article>()
                .orderByDesc(Article::getPublishTime);
        if (title != null && !title.isBlank()) {
            q.like(Article::getTitle, title);
        }
        if (status != null) {
            q.eq(Article::getStatus, status);
        }
        // RBAC：如果是作者角色且不是管理员，则只看自己的文章；否则（管理员或其他角色）可查看全部
        boolean isAdmin = StpUtil.hasRole("admin");
        boolean isAuthor = StpUtil.hasRole("author");
        if (!isAdmin && isAuthor) {
            Long loginId = StpUtil.getLoginIdAsLong();
            q.eq(Article::getUserId, loginId);
        }
        // 按作者（用户名或昵称）模糊搜索
        if (author != null && !author.isBlank()) {
            var users = userMapper.selectList(
                    new LambdaQueryWrapper<User>()
                            .like(User::getUsername, author)
                            .or()
                            .like(User::getNickname, author)
            );
            if (users == null || users.isEmpty()) {
                return Result.ok(PageResult.of(0, current, size, java.util.List.of()));
            }
            java.util.Set<Long> userIds = new java.util.HashSet<>();
            for (User u : users) {
                if (u.getId() != null) {
                    userIds.add(u.getId());
                }
            }
            if (userIds.isEmpty()) {
                return Result.ok(PageResult.of(0, current, size, java.util.List.of()));
            }
            q.in(Article::getUserId, userIds);
        }
        Page<Article> result = articleMapper.selectPage(page, q);
        java.util.List<Article> records = result.getRecords();
        java.util.List<ArticleAdminVO> voList = new java.util.ArrayList<>();
        if (records != null && !records.isEmpty()) {
            java.util.Set<Long> uids = new java.util.HashSet<>();
            for (Article a : records) {
                if (a.getUserId() != null) {
                    uids.add(a.getUserId());
                }
            }
            java.util.Map<Long, User> userMap = java.util.Collections.emptyMap();
            if (!uids.isEmpty()) {
                java.util.List<User> users = userMapper.selectBatchIds(uids);
                userMap = new java.util.HashMap<>();
                if (users != null) {
                    for (User u : users) {
                        if (u.getId() != null) {
                            userMap.put(u.getId(), u);
                        }
                    }
                }
            }
            for (Article a : records) {
                ArticleAdminVO vo = new ArticleAdminVO();
                vo.setId(a.getId());
                vo.setTitle(a.getTitle());
                vo.setSummary(a.getSummary());
                // 封面优先使用 coverImage，没有则从内容中提取第一张图片作为封面
                String cover = a.getCoverImage();
                if ((cover == null || cover.isBlank()) && a.getContent() != null) {
                    cover = extractFirstImageUrl(a.getContent());
                }
                vo.setCoverImage(cover);
                vo.setViewCount(a.getViewCount());
                vo.setLikeCount(a.getLikeCount());
                vo.setCommentCount(a.getCommentCount());
                vo.setStatus(a.getStatus());
                vo.setPublishTime(a.getPublishTime());
                if (a.getUserId() != null && userMap.containsKey(a.getUserId())) {
                    User u = userMap.get(a.getUserId());
                    String name = u.getNickname() != null && !u.getNickname().isBlank()
                            ? u.getNickname()
                            : u.getUsername();
                    vo.setAuthorName(name);
                }
                voList.add(vo);
            }
        }
        PageResult<ArticleAdminVO> pr = PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), voList);
        return Result.ok(pr);
    }

    /**
     * 从文章内容中提取第一张图片的 URL，支持简单 Markdown 语法：![](url)。
     */
    private String extractFirstImageUrl(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        // 匹配 Markdown 图片：![](...)
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("!\\[[^\\]]*]\\((https?[^)]+)\\)");
        java.util.regex.Matcher m = p.matcher(content);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    @Operation(summary = "文章详情")
    @GetMapping("/{id}")
    public Result<Article> detail(@PathVariable Long id) {
        Article a = articleMapper.selectById(id);
        if (a == null) {
            return Result.fail("文章不存在");
        }
        return Result.ok(a);
    }

    @Operation(summary = "文章已关联标签ID列表")
    @GetMapping("/{id}/tags")
    public Result<java.util.List<Long>> tags(@PathVariable Long id) {
        java.util.List<ArticleTag> ats = articleTagMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ArticleTag>()
                        .eq(ArticleTag::getArticleId, id));
        java.util.List<Long> ids = new java.util.ArrayList<>();
        if (ats != null) {
            for (ArticleTag at : ats) {
                if (at.getTagId() != null) {
                    ids.add(at.getTagId());
                }
            }
        }
        return Result.ok(ids);
    }

    @Operation(summary = "新增文章（含标签）")
    @PostMapping
    @Log(module = "文章", operation = "新增文章")
    @Idempotent(expireSeconds = 10)
    public Result<Void> create(@RequestBody java.util.Map<String, Object> body) {
        Article a = new Article();
        Object titleObj = body.get("title");
        Object contentObj = body.get("content");
        if (titleObj == null || titleObj.toString().isBlank() || contentObj == null || contentObj.toString().isBlank()) {
            return Result.fail("标题或内容不能为空");
        }
        // 文章归属：默认归属当前登录用户（管理员创建则归属管理员账号，作者创建则归属作者本人）
        try {
            Long loginId = StpUtil.getLoginIdAsLong();
            a.setUserId(loginId);
        } catch (Exception ignored) {
        }
        a.setTitle(titleObj.toString());
        a.setSummary(body.get("summary") == null ? null : body.get("summary").toString());
        if (body.get("categoryId") != null) {
            a.setCategoryId(Long.valueOf(body.get("categoryId").toString()));
        }
        a.setContent(contentObj.toString());
        a.setCoverImage(body.get("coverImage") == null ? null : body.get("coverImage").toString());
        a.setStatus(body.get("status") == null ? 1 : Integer.valueOf(body.get("status").toString()));
        a.setPublishTime(java.time.LocalDateTime.now());
        a.setCreateTime(java.time.LocalDateTime.now());
        a.setUpdateTime(java.time.LocalDateTime.now());
        articleMapper.insert(a);
        handleTagsForArticle(a.getId(), body.get("tagIds"), body.get("newTags"));
        return Result.ok();
    }

    @Operation(summary = "修改文章（含标签）")
    @PutMapping("/{id}")
    @Log(module = "文章", operation = "修改文章")
    @Idempotent(expireSeconds = 10)
    public Result<Void> update(@PathVariable Long id, @RequestBody java.util.Map<String, Object> body) {
        Article a = articleMapper.selectById(id);
        if (a == null) {
            return Result.fail("文章不存在");
        }
        if (body.get("title") != null) {
            a.setTitle(body.get("title").toString());
        }
        if (body.get("summary") != null) {
            a.setSummary(body.get("summary").toString());
        }
        if (body.get("content") != null) {
            a.setContent(body.get("content").toString());
        }
        if (body.get("categoryId") != null) {
            a.setCategoryId(Long.valueOf(body.get("categoryId").toString()));
        }
        if (body.get("coverImage") != null) {
            a.setCoverImage(body.get("coverImage").toString());
        }
        if (body.get("status") != null) {
            a.setStatus(Integer.valueOf(body.get("status").toString()));
        }
        a.setUpdateTime(java.time.LocalDateTime.now());
        articleMapper.updateById(a);

        // 先清除原有标签关联，再重新绑定
        articleTagMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ArticleTag>()
                .eq(ArticleTag::getArticleId, id));
        handleTagsForArticle(id, body.get("tagIds"), body.get("newTags"));
        return Result.ok();
    }

    @Operation(summary = "修改文章状态（草稿/发布/违规）")
    @PutMapping("/{id}/status")
    @Log(module = "文章", operation = "修改文章状态")
    @Idempotent(expireSeconds = 5)
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Article article = new Article();
        article.setId(id);
        article.setStatus(status);
        articleMapper.updateById(article);
        return Result.ok();
    }

    /**
     * 处理文章标签绑定：包括已有标签ID与新标签名。
     */
    private void handleTagsForArticle(Long articleId, Object tagIdsObj, Object newTagsObj) {
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

        if (!newTagNames.isEmpty()) {
            // 名称去重后再处理，避免重复新增标签
            java.util.Set<String> uniqueNames = new java.util.LinkedHashSet<>(newTagNames);
            for (String name : uniqueNames) {
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
                t.setCreateTime(java.time.LocalDateTime.now());
                tagMapper.insert(t);
                tagIds.add(t.getId());
            }
        }

        if (!tagIds.isEmpty()) {
            for (Long tagId : tagIds) {
                if (tagId == null) continue;
                ArticleTag at = new ArticleTag();
                at.setArticleId(articleId);
                at.setTagId(tagId);
                articleTagMapper.insert(at);
            }
        }
    }
}
