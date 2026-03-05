package com.blog.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.model.entity.UserWallet;
import com.blog.mapper.UserWalletMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 风月币排行榜定时校准任务。
 *
 * <p>每 5 分钟根据 MySQL 中的钱包数据全量重建 Redis 排行，
 * 避免由于历史操作或异常导致 MySQL 与 Redis 数据不一致。
 *
 * @author 一朝风月
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WalletRankSyncTask {

    private static final String KEY_COIN_RANK = "blog:coin:rank";

    private final UserWalletMapper userWalletMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 每 5 分钟执行一次排行榜校准。
     */
    @Scheduled(fixedDelay = 5 * 60 * 1000L)
    public void syncCoinRankFromDb() {
        try {
            // 查询所有钱包记录（如有 deleted 字段可在此处追加过滤条件）
            List<UserWallet> wallets = userWalletMapper.selectList(new LambdaQueryWrapper<UserWallet>());
            // 清空旧数据
            redisTemplate.delete(KEY_COIN_RANK);
            if (wallets == null || wallets.isEmpty()) {
                return;
            }
            for (UserWallet w : wallets) {
                if (w.getUserId() == null) {
                    continue;
                }
                BigDecimal income = w.getTotalIncome() != null ? w.getTotalIncome() : BigDecimal.ZERO;
                if (income.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                redisTemplate.opsForZSet()
                        .add(KEY_COIN_RANK, String.valueOf(w.getUserId()), income.longValue());
            }
        } catch (Exception e) {
            log.warn("同步风月币排行榜失败: {}", e.getMessage());
        }
    }
}

