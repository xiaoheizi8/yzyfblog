package com.blog.controller.admin;

import com.blog.annotation.Log;
import com.blog.common.Result;
import com.blog.model.vo.portal.CoinRankVO;
import com.blog.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 管理端-风月币排行榜。
 *
 * <p>用于仪表盘等处展示用户风月币排行信息。
 *
 * @author 一朝风月
 */
@RestController
@RequestMapping("/admin/coin")
@RequiredArgsConstructor
public class AdminCoinController {

    private final WalletService walletService;

    @Operation(summary = "风月币排行榜")
    @GetMapping("/ranking")
    public Result<List<CoinRankVO>> ranking(@RequestParam(defaultValue = "20") int limit) {
        return Result.ok(walletService.getCoinRanking(limit));
    }

    @Operation(summary = "调整用户风月币余额")
    @PostMapping("/adjust")
    @Log(module = "风月币", operation = "调整用户风月币")
    public Result<Void> adjustBalance(@RequestBody Map<String, Object> params) {
        Object userIdObj = params.get("userId");
        Object amountObj = params.get("amount");
        
        if (userIdObj == null || amountObj == null) {
            return Result.fail("参数不合法");
        }

        Long userId = Long.parseLong(userIdObj.toString());
        BigDecimal amount = new BigDecimal(amountObj.toString());
        String remark = params.get("remark") != null ? params.get("remark").toString() : "管理员调整";

        walletService.adjustBalance(userId, amount, remark);
        return Result.ok();
    }
}

