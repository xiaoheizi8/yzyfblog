package com.blog.controller.portal;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.annotation.Idempotent;
import com.blog.common.Result;
import com.blog.model.entity.Comment;
import com.blog.mapper.CommentMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 门户-评论。
 *
 * <p>文章评论的列表与发布，用于小程序 / H5。
 *
 * @author 一朝风月
 */
@RestController
@RequestMapping("/portal/comment")
@RequiredArgsConstructor
public class PortalCommentController {

    private final CommentMapper commentMapper;

    @Operation(summary = "文章评论列表")
    @GetMapping("/list")
    public Result<List<Comment>> list(@RequestParam Long articleId) {
        List<Comment> list = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getArticleId, articleId)
                        .eq(Comment::getStatus, 1)
                        .orderByAsc(Comment::getCreateTime));
        return Result.ok(list);
    }

    @Operation(summary = "发表评论")
    @PostMapping("/submit")
    @Idempotent(expireSeconds = 5)
    public Result<Comment> submit(@RequestBody Map<String, Object> body) {
        Object articleIdObj = body.get("articleId");
        Object contentObj = body.get("content");
        if (articleIdObj == null || contentObj == null || contentObj.toString().isBlank()) {
            return Result.fail("文章ID或评论内容不能为空");
        }
        Long articleId = Long.valueOf(articleIdObj.toString());
        Long userId = null;
        if (StpUtil.isLogin()) {
            userId = StpUtil.getLoginIdAsLong();
        }
        Long parentId = body.get("parentId") == null ? 0L : Long.valueOf(body.get("parentId").toString());
        Long replyToId = body.get("replyToId") == null ? null : Long.valueOf(body.get("replyToId").toString());

        Comment c = new Comment();
        c.setArticleId(articleId);
        c.setUserId(userId);
        c.setParentId(parentId);
        c.setReplyToId(replyToId);
        c.setContent(contentObj.toString());
        c.setLikeCount(0);
        c.setStatus(1);
        c.setCreateTime(LocalDateTime.now());
        c.setUpdateTime(LocalDateTime.now());

        commentMapper.insert(c);
        return Result.ok(c);
    }
}

