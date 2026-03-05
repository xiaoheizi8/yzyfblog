package com.blog.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.blog.model.entity.Permission;
import com.blog.model.entity.User;
import com.blog.mapper.PermissionMapper;
import com.blog.mapper.UserMapper;
import com.blog.mapper.UserRoleMapper;
import com.blog.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 认证服务：登录、权限、菜单
 *
 * @author blog
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final PermissionMapper permissionMapper;

    @Override
    public User getUserById(Long userId) {
        if (userId == null) return null;
        User u = userMapper.selectById(userId);
        if (u != null) u.setPassword(null);
        return u;
    }

    @Override
    public User login(String username, String password) {
        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username));
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getPassword() == null || !passwordMatches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("账号已禁用");
        }
        StpUtil.login(user.getId());
        user.setPassword(null);
        return user;
    }

    private boolean passwordMatches(String raw, String encoded) {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().matches(raw, encoded);
    }

    @Override
    public List<String> getPermissionCodes(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return userMapper.selectPermissionCodesByUserId(userId);
    }

    @Override
    public List<Permission> getMenuTree(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return permissionMapper.selectMenuTreeByRoleIds(roleIds);
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }
}
