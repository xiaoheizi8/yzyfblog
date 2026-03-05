package com.blog.controller.portal;

import cn.dev33.satoken.stp.StpUtil;
import com.blog.common.Result;
import com.blog.model.entity.UserWallet;
import com.blog.model.vo.portal.CoinRankVO;
import com.blog.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 门户-风月币钱包与排行榜。
 *
 * <p>提供签到、查询钱包、文章打赏、排行榜等接口。
 *
 * @author 一朝风月
 */
@RestController
@RequestMapping("/portal/coin")
@RequiredArgsConstructor
public class PortalCoinController {

    private final WalletService walletService;

    @Operation(summary = "每日签到领取风月币")
    @PostMapping("/sign-in")
    public Result<Void> signIn() {
        Long userId = StpUtil.getLoginIdAsLong();
        walletService.signIn(userId);
        return Result.ok();
    }

    @Operation(summary = "我的钱包")
    @GetMapping("/wallet")
    public Result<UserWallet> wallet() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(walletService.getWallet(userId));
    }

    @Operation(summary = "文章打赏")
    @PostMapping("/article/{articleId}/tip")
    public Result<Void> tip(@PathVariable Long articleId, @RequestBody Map<String, Object> body) {
        Long userId = StpUtil.getLoginIdAsLong();
        Object amountObj = body.get("amount");
        if (amountObj == null) {
            return Result.fail("打赏金额不能为空");
        }
        BigDecimal amount = new BigDecimal(amountObj.toString());
        walletService.tip(userId, articleId, amount);
        return Result.ok();
    }

    @Operation(summary = "风月币排行榜")
    @GetMapping("/ranking")
    public Result<List<CoinRankVO>> ranking(@RequestParam(defaultValue = "10") int limit) {
        return Result.ok(walletService.getCoinRanking(limit));
    }
}

