package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.annotation.Log;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.model.entity.Comment;
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

