package com.blog.service;

import com.blog.model.vo.portal.ArticleLikeRankVO;

import java.util.List;

/**
 * 文章点赞与排行榜（Redis）
 *
 * @author blog
 */
public interface ArticleLikeService {

    /**
     * 点赞（用户每人每篇文章仅计一次）
     *
     * @param articleId 文章ID
     * @param userId    用户ID，匿名可为 null 则用 ip 或设备标识
     * @return true 点赞成功，false 已点过
     */
    boolean like(Long articleId, Long userId);

    /**
     * 取消点赞
     */
    boolean unlike(Long articleId, Long userId);

    /**
     * 是否已点赞
     */
    boolean hasLiked(Long articleId, Long userId);

    /**
     * 获取文章当前点赞数（优先 Redis，无则取 DB 并回写 Redis）
     */
    long getLikeCount(Long articleId);

    /**
     * 点赞排行榜（按点赞数降序）
     *
     * @param limit 条数
     * @return 文章ID、点赞数、标题等
     */
    List<ArticleLikeRankVO> getRanking(int limit);
}
