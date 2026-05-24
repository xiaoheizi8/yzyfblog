package com.blog.controller.portal;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.annotation.Idempotent;
import com.blog.common.Result;
import com.blog.model.entity.Comment;
import com.blog.model.entity.User;
import com.blog.model.vo.portal.CommentVO;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final UserMapper userMapper;

    @Operation(summary = "文章评论列表（含用户昵称）")
    @GetMapping("/list")
    public Result<List<CommentVO>> list(@RequestParam Long articleId) {
        List<Comment> list = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getArticleId, articleId)
                        .eq(Comment::getStatus, 1)
                        .orderByAsc(Comment::getCreateTime));

        List<CommentVO> voList = new ArrayList<>();
        if (!list.isEmpty()) {
            List<Long> userIds = list.stream()
                    .map(Comment::getUserId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, String> userIdToNickname = new java.util.HashMap<>();
            if (!userIds.isEmpty()) {
                List<User> users = userMapper.selectBatchIds(userIds);
                for (User u : users) {
                    if (u != null && u.getId() != null) {
                        String display = (u.getNickname() != null && !u.getNickname().isBlank())
                                ? u.getNickname()
                                : (u.getUsername() != null ? u.getUsername() : "用户" + u.getId());
                        userIdToNickname.put(u.getId(), display);
                    }
                }
            }
            for (Comment c : list) {
                CommentVO vo = new CommentVO();
                vo.setId(c.getId());
                vo.setArticleId(c.getArticleId());
                vo.setUserId(c.getUserId());
                vo.setParentId(c.getParentId());
                vo.setReplyToId(c.getReplyToId());
                vo.setContent(c.getContent());
                vo.setLikeCount(c.getLikeCount());
                vo.setCreateTime(c.getCreateTime());
                if (c.getUserId() != null) {
                    vo.setNickname(userIdToNickname.getOrDefault(c.getUserId(), "用户" + c.getUserId()));
                } else {
                    vo.setNickname("游客");
                }
                voList.add(vo);
            }
        }
        return Result.ok(voList);
    }

    @Operation(summary = "发表评论")
    @PostMapping("/submit")
    @Idempotent(expireSeconds = 5)
    public Result<CommentVO> submit(@RequestBody Map<String, Object> body) {
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
        CommentVO vo = new CommentVO();
        vo.setId(c.getId());
        vo.setArticleId(c.getArticleId());
        vo.setUserId(c.getUserId());
        vo.setParentId(c.getParentId());
        vo.setReplyToId(c.getReplyToId());
        vo.setContent(c.getContent());
        vo.setLikeCount(c.getLikeCount());
        vo.setCreateTime(c.getCreateTime());
        if (c.getUserId() != null) {
            User u = userMapper.selectById(c.getUserId());
            vo.setNickname(u != null && u.getNickname() != null && !u.getNickname().isBlank()
                    ? u.getNickname()
                    : (u != null && u.getUsername() != null ? u.getUsername() : "用户" + c.getUserId()));
        } else {
            vo.setNickname("游客");
        }
        return Result.ok(vo);
    }
}

