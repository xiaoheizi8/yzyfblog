package com.blog.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.model.entity.UserWallet;
import com.blog.mapper.UserWalletMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 风月币排行榜定时同步任务。
 *
 * <p>定期从数据库全量重建 Redis 排行榜，纠正偶发双写不一致问题。
 *
 * @author 一朝风月
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CoinRankingSyncTask {

    private static final String KEY_COIN_RANK = "blog:coin:rank";

    private final UserWalletMapper userWalletMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 每天凌晨 3 点重建排行榜。
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void rebuildCoinRanking() {
        log.info("开始重建风月币排行榜...");
        redisTemplate.delete(KEY_COIN_RANK);
        List<UserWallet> wallets = userWalletMapper.selectList(
                new LambdaQueryWrapper<UserWallet>()
                        .orderByDesc(UserWallet::getTotalIncome));
        for (UserWallet w : wallets) {
            if (w.getUserId() == null || w.getTotalIncome() == null) {
                continue;
            }
            redisTemplate.opsForZSet().add(
                    KEY_COIN_RANK,
                    String.valueOf(w.getUserId()),
                    w.getTotalIncome().longValue());
        }
        log.info("风月币排行榜重建完成，用户数={}", wallets.size());
    }
}

