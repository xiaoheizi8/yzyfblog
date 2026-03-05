package com.blog.service;

import com.blog.model.entity.UserWallet;
import com.blog.model.vo.portal.CoinRankVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 风月币钱包服务。
 *
 * <p>负责签到奖励、发文奖励、点赞奖励、打赏分成以及排行榜等核心逻辑。
 *
 * @author 一朝风月
 */
public interface WalletService {

    /**
     * 获取用户钱包信息，若不存在则视为 0。
     *
     * @param userId 用户ID
     * @return 钱包信息
     */
    UserWallet getWallet(Long userId);

    /**
     * 每日签到领取风月币。
     *
     * @param userId 用户ID
     */
    void signIn(Long userId);

    /**
     * 发表文章奖励。
     *
     * @param userId 作者ID
     */
    void rewardForPost(Long userId);

    /**
     * 文章被点赞奖励。
     *
     * @param userId 作者ID
     */
    void rewardForLike(Long userId);

    /**
     * 打赏文章。
     *
     * @param fromUserId 打赏人
     * @param articleId  文章ID
     * @param amount     打赏金额
     */
    void tip(Long fromUserId, Long articleId, BigDecimal amount);

    /**
     * 风月币排行榜。
     *
     * @param limit 返回前 N 名
     * @return 排行列表
     */
    List<CoinRankVO> getCoinRanking(int limit);
}

