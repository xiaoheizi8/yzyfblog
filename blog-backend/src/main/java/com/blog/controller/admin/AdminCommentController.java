package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.annotation.Log;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.model.entity.Article;
import com.blog.model.entity.Comment;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CommentMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端-评论管理。
 *
 * <p>用于后台查看、删除评论。
 *
 * @author 一朝风月
 */
@RestController
@RequestMapping("/admin/comment")
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;

    @Operation(summary = "评论分页列表")
    @GetMapping("/page")
    public Result<PageResult<Comment>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long articleId) {
        Page<Comment> page = new Page<>(current, size);
        LambdaQueryWrapper<Comment> q = new LambdaQueryWrapper<Comment>()
                .orderByDesc(Comment::getCreateTime);
        if (articleId != null) {
            q.eq(Comment::getArticleId, articleId);
        }
        // RBAC：如果是作者角色且不是管理员，则只看自己文章下的评论；否则（管理员或其他角色）可查看全部
        boolean isAdmin = cn.dev33.satoken.stp.StpUtil.hasRole("admin");
        boolean isAuthor = cn.dev33.satoken.stp.StpUtil.hasRole("author");
        if (!isAdmin && isAuthor) {
            Long loginId = cn.dev33.satoken.stp.StpUtil.getLoginIdAsLong();
            java.util.List<Article> articles = articleMapper.selectList(
                    new LambdaQueryWrapper<Article>().eq(Article::getUserId, loginId));
            if (articles == null || articles.isEmpty()) {
                PageResult<Comment> empty = PageResult.of(0, current, size, java.util.List.of());
                return Result.ok(empty);
            }
            java.util.Set<Long> articleIds = new java.util.HashSet<>();
            for (Article a : articles) {
                if (a.getId() != null) {
                    articleIds.add(a.getId());
                }
            }
            if (articleIds.isEmpty()) {
                PageResult<Comment> empty = PageResult.of(0, current, size, java.util.List.of());
                return Result.ok(empty);
            }
            q.in(Comment::getArticleId, articleIds);
        }
        Page<Comment> result = commentMapper.selectPage(page, q);
        PageResult<Comment> pr = PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
        return Result.ok(pr);
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    @Log(module = "评论", operation = "删除评论")
    public Result<Void> delete(@PathVariable Long id) {
        commentMapper.deleteById(id);
        return Result.ok();
    }
}

