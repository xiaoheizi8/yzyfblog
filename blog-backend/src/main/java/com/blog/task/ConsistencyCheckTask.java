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
 * 数据一致性校验定时任务。
 *
 * <p>定期校验数据库与 Redis 排行榜数据的一致性，发现不一致时记录日志并自动修复。
 *
 * @author 一朝风月
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConsistencyCheckTask {

    private static final String KEY_COIN_RANK = "blog:coin:rank";
    /** 不一致阈值：超过此数量触发告警 */
    private static final int INCONSISTENT_THRESHOLD = 10;

    private final UserWalletMapper userWalletMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 每 2 小时校验一次数据库与 Redis 排行榜的一致性。
     */
    @Scheduled(cron = "0 0 */2 * * ?")
    public void checkCoinRankConsistency() {
        log.info("开始校验风月币排行榜数据一致性...");
        try {
            // 获取 DB 前 100 名
            List<UserWallet> dbTop100 = userWalletMapper.selectList(
                    new LambdaQueryWrapper<UserWallet>()
                            .isNotNull(UserWallet::getTotalIncome)
                            .orderByDesc(UserWallet::getTotalIncome)
                            .last("LIMIT 100"));

            int inconsistentCount = 0;
            int fixedCount = 0;

            for (UserWallet wallet : dbTop100) {
                if (wallet.getUserId() == null || wallet.getTotalIncome() == null) {
                    continue;
                }

                Double redisScore = redisTemplate.opsForZSet().score(
                        KEY_COIN_RANK, String.valueOf(wallet.getUserId()));

                long dbValue = wallet.getTotalIncome().longValue();
                long redisValue = redisScore != null ? redisScore.longValue() : -1;

                // 比较差异（允许 1 的误差，因为可能是并发导致的时间差）
                if (redisScore == null || Math.abs(redisValue - dbValue) > 1) {
                    log.warn("数据不一致: userId={}, DB={}, Redis={}",
                            wallet.getUserId(), dbValue, redisScore);
                    inconsistentCount++;

                    // 自动修复：用数据库的值覆盖 Redis
                    try {
                        redisTemplate.opsForZSet().add(
                                KEY_COIN_RANK,
                                String.valueOf(wallet.getUserId()),
                                dbValue);
                        fixedCount++;
                        log.info("自动修复不一致数据: userId={}, 正确值={}", wallet.getUserId(), dbValue);
                    } catch (Exception e) {
                        log.error("自动修复失败: userId={}", wallet.getUserId(), e);
                    }
                }
            }

            // 检查 Redis 中存在但 DB 中不存在的用户
            checkOrphanedRedisData(dbTop100);

            log.info("一致性校验完成，不一致数量: {}, 已修复: {}", inconsistentCount, fixedCount);

            // 超过阈值触发告警
            if (inconsistentCount > INCONSISTENT_THRESHOLD) {
                log.error("⚠️ 数据不一致超过阈值 {}，当前不一致数量: {}，请检查系统！",
                        INCONSISTENT_THRESHOLD, inconsistentCount);
                // TODO: 可以在此处集成钉钉/企业微信/邮件告警
            }
        } catch (Exception e) {
            log.error("数据一致性校验失败", e);
        }
    }

    /**
     * 检查 Redis 中存在但数据库中已不存在的孤儿数据。
     */
    private void checkOrphanedRedisData(List<UserWallet> dbWallets) {
        try {
            // 获取 Redis 中的所有用户 ID
            var redisAllKeys = redisTemplate.opsForZSet().reverseRange(KEY_COIN_RANK, 0, -1);
            if (redisAllKeys == null || redisAllKeys.isEmpty()) {
                return;
            }

            // 获取 DB 中的所有用户 ID
            var dbUserIds = dbWallets.stream()
                    .map(w -> String.valueOf(w.getUserId()))
                    .collect(java.util.stream.Collectors.toSet());

            // 检查孤儿数据（只检查前 100 名范围内的）
            int orphanCount = 0;
            for (Object redisKey : redisAllKeys) {
                if (!dbUserIds.contains(redisKey.toString())) {
                    // 可能是 DB 查询限制 100 条导致的，需要进一步确认
                    Long userId = Long.parseLong(redisKey.toString());
                    UserWallet wallet = userWalletMapper.selectOne(
                            new LambdaQueryWrapper<UserWallet>()
                                    .eq(UserWallet::getUserId, userId));

                    if (wallet == null || wallet.getTotalIncome() == null) {
                        // 数据库中确实不存在，清理 Redis 数据
                        redisTemplate.opsForZSet().remove(KEY_COIN_RANK, redisKey.toString());
                        orphanCount++;
                        log.info("清理孤儿数据: userId={}", redisKey);
                    }
                }
            }

            if (orphanCount > 0) {
                log.info("共清理 {} 条孤儿数据", orphanCount);
            }
        } catch (Exception e) {
            log.error("检查孤儿数据失败", e);
        }
    }
}
