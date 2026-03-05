package com.blog.controller.portal;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.Result;
import com.blog.model.entity.Article;
import com.blog.model.entity.UserFavorite;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.UserFavoriteMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 门户-用户收藏（文章）
 *
 * @author 一朝风月
 */
@RestController
@RequestMapping("/portal/favorite")
@RequiredArgsConstructor
public class PortalFavoriteController {

    private final UserFavoriteMapper userFavoriteMapper;
    private final ArticleMapper articleMapper;

    @Operation(summary = "收藏列表（当前用户）")
    @GetMapping("/list")
    public Result<List<Article>> list() {
        if (!StpUtil.isLogin()) {
            return Result.fail("请先登录");
        }
        Long userId = StpUtil.getLoginIdAsLong();
        List<UserFavorite> favorites = userFavoriteMapper.selectList(
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .orderByDesc(UserFavorite::getCreateTime)
        );
        if (favorites == null || favorites.isEmpty()) {
            return Result.ok(new ArrayList<>());
        }
        List<Long> articleIds = favorites.stream().map(UserFavorite::getArticleId).distinct().collect(Collectors.toList());
        List<Article> articles = articleMapper.selectBatchIds(articleIds);
        Map<Long, Article> map = articles.stream().collect(Collectors.toMap(Article::getId, a -> a));
        List<Article> ordered = new ArrayList<>();
        for (Long id : articleIds) {
            Article a = map.get(id);
            if (a != null) {
                ordered.add(a);
            }
        }
        return Result.ok(ordered);
    }

    @Operation(summary = "是否已收藏")
    @GetMapping("/check")
    public Result<Boolean> check(@RequestParam Long articleId) {
        if (!StpUtil.isLogin()) {
            return Result.ok(false);
        }
        Long userId = StpUtil.getLoginIdAsLong();
        long count = userFavoriteMapper.selectCount(
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .eq(UserFavorite::getArticleId, articleId)
        );
        return Result.ok(count > 0);
    }

    @Operation(summary = "添加收藏")
    @PostMapping("/add")
    public Result<Void> add(@RequestBody Map<String, Object> body) {
        if (!StpUtil.isLogin()) {
            return Result.fail("请先登录");
        }
        Object aid = body.get("articleId");
        if (aid == null) {
            return Result.fail("articleId 不能为空");
        }
        Long articleId = Long.valueOf(aid.toString());
        if (articleMapper.selectById(articleId) == null) {
            return Result.fail("文章不存在");
        }
        Long userId = StpUtil.getLoginIdAsLong();
        long count = userFavoriteMapper.selectCount(
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .eq(UserFavorite::getArticleId, articleId)
        );
        if (count > 0) {
            return Result.ok();
        }
        UserFavorite f = new UserFavorite();
        f.setUserId(userId);
        f.setArticleId(articleId);
        f.setCreateTime(java.time.LocalDateTime.now());
        userFavoriteMapper.insert(f);
        return Result.ok();
    }

    @Operation(summary = "取消收藏")
    @PostMapping("/remove")
    public Result<Void> remove(@RequestBody Map<String, Object> body) {
        if (!StpUtil.isLogin()) {
            return Result.fail("请先登录");
        }
        Object aid = body.get("articleId");
        if (aid == null) {
            return Result.fail("articleId 不能为空");
        }
        Long articleId = Long.valueOf(aid.toString());
        Long userId = StpUtil.getLoginIdAsLong();
        userFavoriteMapper.delete(
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .eq(UserFavorite::getArticleId, articleId)
        );
        return Result.ok();
    }
}
