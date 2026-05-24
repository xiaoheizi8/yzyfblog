package com.blog.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.model.entity.UserWallet;
import com.blog.mapper.UserWalletMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 风月币排行榜定时同步任务。
 *
 * <p>支持增量同步与全量重建两种模式：
 * <ul>
 *   <li>增量同步：每 10 分钟执行一次，仅同步最近有变更的用户，降低不一致窗口</li>
 *   <li>全量重建：每天凌晨 3 点执行，从数据库全量重建 Redis 排行榜，纠正累积的不一致</li>
 * </ul>
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
     * 每 10 分钟增量同步最近 15 分钟内变更的钱包数据。
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void incrementalSyncCoinRanking() {
        log.info("开始增量同步风月币排行榜...");
        try {
            // 同步最近 15 分钟内有变更的数据
            LocalDateTime since = LocalDateTime.now().minusMinutes(15);
            List<UserWallet> changedWallets = userWalletMapper.selectList(
                    new LambdaQueryWrapper<UserWallet>()
                            .ge(UserWallet::getUpdateTime, since)
                            .isNotNull(UserWallet::getTotalIncome));

            int syncCount = 0;
            for (UserWallet w : changedWallets) {
                if (w.getUserId() != null && w.getTotalIncome() != null) {
                    redisTemplate.opsForZSet().add(
                            KEY_COIN_RANK,
                            String.valueOf(w.getUserId()),
                            w.getTotalIncome().longValue());
                    syncCount++;
                }
            }
            log.info("增量同步完成，同步用户数={}", syncCount);
        } catch (Exception e) {
            log.error("增量同步风月币排行榜失败", e);
        }
    }

    /**
     * 每天凌晨 3 点全量重建排行榜。
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void rebuildCoinRanking() {
        log.info("开始全量重建风月币排行榜...");
        try {
            // 使用原子操作逐个更新，避免删除整个键导致短暂查询失败
            List<UserWallet> wallets = userWalletMapper.selectList(
                    new LambdaQueryWrapper<UserWallet>()
                            .isNotNull(UserWallet::getTotalIncome)
                            .orderByDesc(UserWallet::getTotalIncome));
            
            // 先获取现有所有键，然后更新
            Set<Object> existingKeysObj = redisTemplate.opsForZSet().reverseRange(KEY_COIN_RANK, 0, -1);
            Set<String> existingKeys = existingKeysObj != null ? 
                    existingKeysObj.stream().map(Object::toString).collect(java.util.stream.Collectors.toSet()) : 
                    java.util.Collections.emptySet();
            
            // 更新或新增数据库中的用户
            for (UserWallet w : wallets) {
                if (w.getUserId() == null || w.getTotalIncome() == null) {
                    continue;
                }
                redisTemplate.opsForZSet().add(
                        KEY_COIN_RANK,
                        String.valueOf(w.getUserId()),
                        w.getTotalIncome().longValue());
            }
            
            // 删除数据库中已不存在的用户（如果有的话）
            if (existingKeys != null) {
                Set<String> dbUserIds = wallets.stream()
                        .map(w -> String.valueOf(w.getUserId()))
                        .collect(java.util.stream.Collectors.toSet());
                
                for (String existingKey : existingKeys) {
                    if (!dbUserIds.contains(existingKey)) {
                        redisTemplate.opsForZSet().remove(KEY_COIN_RANK, existingKey);
                        log.debug("清理无效用户: {}", existingKey);
                    }
                }
            }
            
            log.info("风月币排行榜全量重建完成，用户数={}", wallets.size());
        } catch (Exception e) {
            log.error("全量重建风月币排行榜失败", e);
        }
    }
}

