package com.blog.controller.portal;

import com.blog.common.Result;
import com.blog.model.vo.portal.ArticleLikeRankVO;
import com.blog.service.ArticleLikeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 门户-文章点赞与排行榜（Redis）
 *
 * @author blog
 */
@RestController
@RequestMapping("/portal/article")
@RequiredArgsConstructor
public class PortalArticleLikeController {

    private final ArticleLikeService articleLikeService;

    @Operation(summary = "点赞")
    @PostMapping("/{id}/like")
    public Result<Map<String, Object>> like(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        Long userId = body != null && body.get("userId") != null
                ? Long.valueOf(body.get("userId").toString()) : null;
        boolean ok = articleLikeService.like(id, userId);
        long count = articleLikeService.getLikeCount(id);
        return Result.ok(Map.of("liked", ok, "likeCount", count));
    }

    @Operation(summary = "取消点赞")
    @PostMapping("/{id}/unlike")
    public Result<Map<String, Object>> unlike(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        Long userId = body != null && body.get("userId") != null
                ? Long.valueOf(body.get("userId").toString()) : null;
        boolean ok = articleLikeService.unlike(id, userId);
        long count = articleLikeService.getLikeCount(id);
        return Result.ok(Map.of("unliked", ok, "likeCount", count));
    }

    @Operation(summary = "是否已点赞")
    @GetMapping("/{id}/liked")
    public Result<Boolean> liked(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        return Result.ok(articleLikeService.hasLiked(id, userId));
    }

    @Operation(summary = "点赞数")
    @GetMapping("/{id}/like-count")
    public Result<Long> likeCount(@PathVariable Long id) {
        return Result.ok(articleLikeService.getLikeCount(id));
    }

    @Operation(summary = "点赞排行榜")
    @GetMapping("/ranking")
    public Result<List<ArticleLikeRankVO>> ranking(@RequestParam(defaultValue = "10") int limit) {
        return Result.ok(articleLikeService.getRanking(limit));
    }
}
