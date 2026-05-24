package com.blog.service.impl;

import com.blog.model.entity.Article;
import com.blog.mapper.ArticleMapper;
import com.blog.model.vo.portal.ArticleLikeRankVO;
import com.blog.service.ArticleLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 点赞与排行榜：ZSET 存排名，SET 存每篇文章的点赞用户防重复
 *
 * @author blog
 */
@Service
@RequiredArgsConstructor
public class ArticleLikeServiceImpl implements ArticleLikeService {

    private static final String KEY_RANK = "blog:article:like:rank";
    private static final String KEY_USER_LIKED = "blog:article:like:users:";
    private static final long RANK_EXPIRE_DAYS = 0; // 不过期

    private final RedisTemplate<String, Object> redisTemplate;
    private final ArticleMapper articleMapper;

    @Override
    public boolean like(Long articleId, Long userId) {
        if (articleId == null) {
            return false;
        }
        String userKey = KEY_USER_LIKED + articleId;
        String uid = userId != null ? String.valueOf(userId) : "anon";
        // Redis SADD 返回添加成功的个数，0 表示该用户之前已经点过赞
        Long added = redisTemplate.opsForSet().add(userKey, uid);
        if (added == null || added == 0L) {
            return false; // 已点过
        }
        redisTemplate.opsForZSet().incrementScore(KEY_RANK, String.valueOf(articleId), 1);
        if (RANK_EXPIRE_DAYS > 0) {
            redisTemplate.expire(KEY_RANK, RANK_EXPIRE_DAYS, TimeUnit.DAYS);
        }
        return true;
    }

    @Override
    public boolean unlike(Long articleId, Long userId) {
        if (articleId == null) return false;
        String userKey = KEY_USER_LIKED + articleId;
        String uid = userId != null ? String.valueOf(userId) : "anon";
        Long removed = redisTemplate.opsForSet().remove(userKey, uid);
        if (Long.valueOf(0).equals(removed)) return false;
        redisTemplate.opsForZSet().incrementScore(KEY_RANK, String.valueOf(articleId), -1);
        return true;
    }

    @Override
    public boolean hasLiked(Long articleId, Long userId) {
        if (articleId == null) return false;
        String uid = userId != null ? String.valueOf(userId) : "anon";
        Boolean member = redisTemplate.opsForSet().isMember(KEY_USER_LIKED + articleId, uid);
        return Boolean.TRUE.equals(member);
    }

    @Override
    public long getLikeCount(Long articleId) {
        if (articleId == null) return 0;
        Double score = redisTemplate.opsForZSet().score(KEY_RANK, String.valueOf(articleId));
        if (score != null) return score.longValue();
        Article a = articleMapper.selectById(articleId);
        if (a != null && a.getLikeCount() != null) {
            redisTemplate.opsForZSet().add(KEY_RANK, String.valueOf(articleId), a.getLikeCount());
            return a.getLikeCount();
        }
        return 0;
    }

    /**
     * 基于 ZSET 获取文章点赞数排名
     * @param limit 条数
     * @return
     */
    @Override
    public List<ArticleLikeRankVO> getRanking(int limit) {
        Set<ZSetOperations.TypedTuple<Object>> set = redisTemplate.opsForZSet()
                .reverseRangeWithScores(KEY_RANK, 0, limit - 1);
        if (set == null || set.isEmpty()) {
            return fallbackRankingFromDb(limit);
        }
        List<ArticleLikeRankVO> list = new ArrayList<>();
        int rank = 1;
        for (ZSetOperations.TypedTuple<Object> t : set) {
            if (t.getValue() == null) continue;
            Long id = Long.parseLong(t.getValue().toString());
            long score = t.getScore() != null ? t.getScore().longValue() : 0;
            Article a = articleMapper.selectById(id);
            list.add(new ArticleLikeRankVO(id, a != null ? a.getTitle() : "", score, rank++));
        }
        return list;
    }

    /**
     * 降级从数据库获取数据
     * @param limit 条数
     * @return
     */
    private List<ArticleLikeRankVO> fallbackRankingFromDb(int limit) {
        List<Article> list = articleMapper.selectRecommend(null, limit);
        List<ArticleLikeRankVO> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Article a = list.get(i);
            result.add(new ArticleLikeRankVO(a.getId(), a.getTitle(),
                    a.getLikeCount() != null ? a.getLikeCount().longValue() : 0, i + 1));
        }
        return result;
    }
}
