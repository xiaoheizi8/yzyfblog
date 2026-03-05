package com.blog.controller.admin;

import cn.dev33.satoken.stp.StpUtil;
import com.blog.annotation.Log;
import com.blog.common.Result;
import com.blog.model.entity.Permission;
import com.blog.model.entity.User;
import com.blog.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理端-认证（登录、权限、菜单）
 *
 * @author blog
 */
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AuthService authService;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        User user = authService.login(username, password);
        StpUtil.getSession().set("user", user);
        String token = StpUtil.getTokenValue();
        return Result.ok(Map.of("token", token, "user", user));
    }

    @Operation(summary = "登出")
    @PostMapping("/logout")
    @Log(module = "认证", operation = "登出")
    public Result<Void> logout() {
        authService.logout();
        return Result.ok();
    }

    @Operation(summary = "当前用户信息")
    @GetMapping("/info")
    public Result<User> info() {
        long userId = StpUtil.getLoginIdAsLong();
        User u = (User) StpUtil.getSession().get("user");
        if (u == null) {
            u = authService.getUserById(userId);
            if (u != null) StpUtil.getSession().set("user", u);
        }
        return Result.ok(u);
    }

    @Operation(summary = "权限编码列表")
    @GetMapping("/permissions")
    public Result<List<String>> permissions() {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(authService.getPermissionCodes(userId));
    }

    @Operation(summary = "菜单树（动态菜单）")
    @GetMapping("/menus")
    public Result<List<Permission>> menus() {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(authService.getMenuTree(userId));
    }
}
