package com.blog.controller.portal;

import cn.dev33.satoken.stp.StpUtil;
import com.blog.common.Result;
import com.blog.model.entity.User;
import com.blog.mapper.UserMapper;
import com.blog.model.vo.portal.UserProfileVO;
import com.blog.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 门户-认证（用户名密码注册、登录）。
 *
 * <p>供小程序 / H5 使用，基于 Sa-Token 维护会话，与管理端共用用户表。
 *
 * @author 一朝风月
 */
@RestController
@RequestMapping("/portal/auth")
@RequiredArgsConstructor
public class PortalAuthController {

    private final UserMapper userMapper;
    private final AuthService authService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Operation(summary = "注册（用户名+密码）")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String nickname = body.getOrDefault("nickname", username);
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return Result.fail("用户名或密码不能为空");
        }
        // 检查是否已存在
        Long exists = userMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username));
        if (exists != null && exists > 0) {
            return Result.fail("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setStatus(1);
        userMapper.insert(user);
        return Result.ok();
    }

    @Operation(summary = "登录（用户名+密码）")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        User user = authService.login(username, password);
        // 与管理端保持一致，使用 Sa-Token 的 token 值
        String token = StpUtil.getTokenValue();
        Map<String, Object> resp = new HashMap<>(4);
        resp.put("token", token);
        resp.put("user", user);
        return Result.ok(resp);
    }

    @Operation(summary = "当前登录用户信息")
    @GetMapping("/info")
    public Result<User> info() {
        long userId = StpUtil.getLoginIdAsLong();
        User u = authService.getUserById(userId);
        return Result.ok(u);
    }

    @Operation(summary = "当前登录用户个人资料")
    @GetMapping("/profile")
    public Result<UserProfileVO> profile() {
        long userId = StpUtil.getLoginIdAsLong();
        User u = authService.getUserById(userId);
        if (u == null) {
            return Result.fail("用户不存在");
        }
        UserProfileVO vo = new UserProfileVO();
        vo.setId(u.getId());
        vo.setUsername(u.getUsername());
        vo.setNickname(u.getNickname());
        vo.setAvatar(u.getAvatar());
        vo.setEmail(u.getEmail());
        vo.setPhone(u.getPhone());
        vo.setCreateTime(u.getCreateTime());
        return Result.ok(vo);
    }

    @Operation(summary = "修改个人资料（昵称、头像、邮箱、手机号）")
    @PutMapping("/profile")
    public Result<UserProfileVO> updateProfile(@RequestBody Map<String, Object> body) {
        long userId = StpUtil.getLoginIdAsLong();
        User u = authService.getUserById(userId);
        if (u == null) {
            return Result.fail("用户不存在");
        }
        if (body.containsKey("nickname")) {
            Object nickname = body.get("nickname");
            u.setNickname(nickname != null ? nickname.toString() : null);
        }
        if (body.containsKey("avatar")) {
            Object avatar = body.get("avatar");
            u.setAvatar(avatar != null ? avatar.toString() : null);
        }
        if (body.containsKey("email")) {
            Object email = body.get("email");
            u.setEmail(email != null ? email.toString() : null);
        }
        if (body.containsKey("phone")) {
            Object phone = body.get("phone");
            u.setPhone(phone != null ? phone.toString() : null);
        }
        userMapper.updateById(u);
        UserProfileVO vo = new UserProfileVO();
        vo.setId(u.getId());
        vo.setUsername(u.getUsername());
        vo.setNickname(u.getNickname());
        vo.setAvatar(u.getAvatar());
        vo.setEmail(u.getEmail());
        vo.setPhone(u.getPhone());
        vo.setCreateTime(u.getCreateTime());
        return Result.ok(vo);
    }
}

