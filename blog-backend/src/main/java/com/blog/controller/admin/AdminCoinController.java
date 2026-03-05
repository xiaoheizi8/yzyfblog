package com.blog.controller.admin;

import com.blog.common.Result;
import com.blog.model.vo.portal.CoinRankVO;
import com.blog.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}

