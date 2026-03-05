package com.blog.controller.portal;

import com.blog.common.Result;
import com.blog.model.entity.User;
import com.blog.service.OAuthService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 第三方登录入口（Gitee/GitHub/QQ 等，减少注册成本）
 *
 * @author blog
 */
@RestController
@RequestMapping("/portal/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oAuthService;

    @Operation(summary = "获取授权地址")
    @GetMapping("/url/{source}")
    public Result<String> authUrl(@PathVariable String source) {
        String url = oAuthService.getAuthorizeUrl(source);
        return Result.ok(url);
    }

    @Operation(summary = "回调登录并返回 token")
    @GetMapping("/callback/{source}")
    public Result<Map<String, Object>> callback(
            @PathVariable String source,
            @RequestParam String code,
            @RequestParam(required = false) String state) {
        User user = oAuthService.loginByCode(source, code, state);
        return Result.ok(Map.of("token", user.getNickname(), "user", user)); // token 实际应由 Sa-Token 颁发
    }
}
