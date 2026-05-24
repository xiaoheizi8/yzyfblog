package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.model.entity.Article;
import com.blog.model.entity.ArticleTip;
import com.blog.model.entity.UserWallet;
import com.blog.model.entity.WalletTransaction;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.ArticleTipMapper;
import com.blog.mapper.UserMapper;
import com.blog.mapper.UserWalletMapper;
import com.blog.mapper.WalletTransactionMapper;
import com.blog.model.vo.portal.CoinRankVO;
import com.blog.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 风月币钱包服务实现。
 *
 * <p>采用 MySQL 作为真实来源，Redis 用于排行榜缓存与签到去重，支持简单的双写与定时校正。
 *
 * @author 一朝风月
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private static final String KEY_COIN_RANK = "blog:coin:rank";
    private static final String KEY_SIGN_PREFIX = "blog:coin:sign:";

    /** 签到奖励 */
    private static final BigDecimal SIGN_IN_REWARD = BigDecimal.valueOf(5);
    /** 发文奖励（每天第一篇文章奖励 5 风月币） */
    private static final BigDecimal POST_REWARD = BigDecimal.valueOf(5);
    /** 被点赞奖励 */
    private static final BigDecimal LIKE_REWARD = BigDecimal.valueOf(1);
    /** 打赏分成比例（给作者） */
    private static final BigDecimal TIP_RATE = BigDecimal.valueOf(0.6);

    private final UserWalletMapper userWalletMapper;
    private final WalletTransactionMapper walletTransactionMapper;
    private final ArticleTipMapper articleTipMapper;
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final com.blog.websocket.BlogWebSocketHandler webSocketHandler;

    @Override
    public UserWallet getWallet(Long userId) {
        if (userId == null) {
            return null;
        }
        UserWallet wallet = userWalletMapper.selectOne(
                new LambdaQueryWrapper<UserWallet>().eq(UserWallet::getUserId, userId));
        if (wallet == null) {
            wallet = new UserWallet();
            wallet.setUserId(userId);
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setTotalIncome(BigDecimal.ZERO);
            wallet.setTotalExpense(BigDecimal.ZERO);
        }
        return wallet;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void signIn(Long userId) {
        if (userId == null) {
            return;
        }
        String key = KEY_SIGN_PREFIX + LocalDate.now();
        Long added = redisTemplate.opsForSet().add(key, String.valueOf(userId));
        // Redis SADD 返回添加成功的个数，0 表示已存在
        if (added == null || added == 0L) {
            // 已签到
            return;
        }
        increaseBalance(userId, SIGN_IN_REWARD, "SIGN_IN", "每日签到奖励");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rewardForPost(Long userId) {
        if (userId == null) {
            return;
        }
        // 每天第一篇文章才奖励：通过当天是否已有 POST 流水判断
        LocalDate today = LocalDate.now();
        long count = walletTransactionMapper.selectCount(
                new LambdaQueryWrapper<WalletTransaction>()
                        .eq(WalletTransaction::getUserId, userId)
                        .eq(WalletTransaction::getType, "POST")
                        .ge(WalletTransaction::getCreateTime, today.atStartOfDay())
        );
        if (count > 0) {
            return;
        }
        increaseBalance(userId, POST_REWARD, "POST", "每日首篇文章奖励");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rewardForLike(Long userId) {
        if (userId == null) {
            return;
        }
        increaseBalance(userId, LIKE_REWARD, "LIKE_REWARD", "文章被点赞奖励");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void tip(Long fromUserId, Long articleId, BigDecimal amount) {
        if (fromUserId == null || articleId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("打赏参数不合法");
        }
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new IllegalArgumentException("文章不存在");
        }
        Long toUserId = article.getUserId();
        if (toUserId == null) {
            throw new IllegalArgumentException("文章作者不存在");
        }
        // 扣减打赏人余额
        changeBalance(fromUserId, amount.negate(), "TIP_OUT", "打赏文章支出");
        // 作者收入
        BigDecimal income = amount.multiply(TIP_RATE).setScale(2, BigDecimal.ROUND_HALF_UP);
        changeBalance(toUserId, income, "TIP_IN", "文章被打赏收入");

        ArticleTip tip = new ArticleTip();
        tip.setFromUserId(fromUserId);
        tip.setToUserId(toUserId);
        tip.setArticleId(articleId);
        tip.setAmount(amount);
        tip.setRealIncome(income);
        articleTipMapper.insert(tip);
    }

    @Override
    public List<CoinRankVO> getCoinRanking(int limit) {
        Set<ZSetOperations.TypedTuple<Object>> set = redisTemplate.opsForZSet()
                .reverseRangeWithScores(KEY_COIN_RANK, 0, limit - 1);
        List<CoinRankVO> list = new ArrayList<>();
        if (set == null || set.isEmpty()) {
            // 直接从 DB 取前 N 名
            List<UserWallet> wallets = userWalletMapper.selectList(
                    new LambdaQueryWrapper<UserWallet>()
                            .orderByDesc(UserWallet::getTotalIncome)
                            .last("LIMIT " + limit));
            int rank = 1;
            for (UserWallet w : wallets) {
                var user = userMapper.selectById(w.getUserId());
                list.add(new CoinRankVO(
                        w.getUserId(),
                        user != null ? (user.getNickname() != null ? user.getNickname() : user.getUsername()) : "",
                        user != null ? user.getAvatar() : null,
                        w.getTotalIncome() != null ? w.getTotalIncome().longValue() : 0L,
                        rank++));
            }
            return list;
        }
        int rank = 1;
        for (ZSetOperations.TypedTuple<Object> t : set) {
            if (t.getValue() == null) {
                continue;
            }
            Long userId = Long.parseLong(t.getValue().toString());
            long coin = t.getScore() != null ? t.getScore().longValue() : 0L;
            var user = userMapper.selectById(userId);
            list.add(new CoinRankVO(
                    userId,
                    user != null ? (user.getNickname() != null ? user.getNickname() : user.getUsername()) : "",
                    user != null ? user.getAvatar() : null,
                    coin,
                    rank++));
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustBalance(Long userId, BigDecimal amount, String remark) {
        if (userId == null || amount == null) {
            throw new IllegalArgumentException("参数不合法");
        }
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            return; // 调整金额为0，直接返回
        }

        String type = amount.compareTo(BigDecimal.ZERO) > 0 ? "ADJUST_IN" : "ADJUST_OUT";
        String finalRemark = (remark != null ? remark : "管理员调整") + " (" + (amount.compareTo(BigDecimal.ZERO) > 0 ? "增发" : "扣减") + ")";

        changeBalance(userId, amount, type, finalRemark);

        log.info("管理员调整用户风月币: userId={}, amount={}, type={}", userId, amount, type);

        // 通过 WebSocket 推送通知给用户
        try {
            UserWallet wallet = getWallet(userId);
            var user = userMapper.selectById(userId);
            String nickname = user != null ? (user.getNickname() != null ? user.getNickname() : user.getUsername()) : "用户";

            Map<String, Object> notification = new java.util.HashMap<>();
            notification.put("type", "COIN_NOTIFICATION");
            notification.put("title", "风月币变动通知");
            notification.put("content", String.format("您的风月币%s %s，当前余额：%s。原因：%s",
                    amount.compareTo(BigDecimal.ZERO) > 0 ? "增加" : "减少",
                    amount.abs(),
                    wallet.getBalance(),
                    finalRemark));
            notification.put("amount", amount);
            notification.put("balance", wallet.getBalance());
            notification.put("time", java.time.LocalDateTime.now().toString());

            webSocketHandler.sendNotification(userId, notification);
        } catch (Exception e) {
            log.error("推送风月币通知失败: userId={}", userId, e);
        }
    }

    private void increaseBalance(Long userId, BigDecimal amount, String type, String remark) {
        changeBalance(userId, amount, type, remark);
    }

    /**
     * 余额增减 + 流水记录 + 排行榜更新（仅对收入部分计入排行）。
     */
    private void changeBalance(Long userId, BigDecimal amount, String type, String remark) {
        UserWallet wallet = userWalletMapper.selectOne(
                new LambdaQueryWrapper<UserWallet>().eq(UserWallet::getUserId, userId));
        if (wallet == null) {
            wallet = new UserWallet();
            wallet.setUserId(userId);
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setTotalIncome(BigDecimal.ZERO);
            wallet.setTotalExpense(BigDecimal.ZERO);
            userWalletMapper.insert(wallet);
        }
        BigDecimal newBalance = wallet.getBalance().add(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("风月币余额不足");
        }
        wallet.setBalance(newBalance);
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            wallet.setTotalIncome(wallet.getTotalIncome().add(amount));
        } else if (amount.compareTo(BigDecimal.ZERO) < 0) {
            wallet.setTotalExpense(wallet.getTotalExpense().add(amount.abs()));
        }
        userWalletMapper.updateById(wallet);

        WalletTransaction tx = new WalletTransaction();
        tx.setUserId(userId);
        tx.setAmount(amount);
        tx.setType(type);
        tx.setRemark(remark);
        walletTransactionMapper.insert(tx);

        // 更新排行榜：仅统计收入部分，带重试机制
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            updateRedisWithRetry(KEY_COIN_RANK, String.valueOf(userId), amount.longValue(), 3);
        }
    }

    /**
     * 带重试机制的 Redis 更新。
     *
     * @param key         Redis key
     * @param userId      用户ID
     * @param score       分数增量
     * @param maxRetries  最大重试次数
     */
    private void updateRedisWithRetry(String key, String userId, long score, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                redisTemplate.opsForZSet().incrementScore(key, userId, score);
                log.debug("Redis排行榜更新成功: userId={}, score={}", userId, score);
                return; // 成功则返回
            } catch (Exception e) {
                log.warn("Redis排行榜更新失败，第{}次重试: userId={}", i + 1, userId, e);
                if (i == maxRetries - 1) {
                    log.error("Redis排行榜更新失败，已达最大重试次数 {}: userId={}", maxRetries, userId);
                    // 记录到日志，定时任务会校正
                }
                try {
                    // 递增延迟重试：100ms, 200ms, 300ms
                    Thread.sleep(100 * (i + 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("重试等待被中断", ie);
                }
            }
        }
    }
}

