package com.blog.service;

import com.blog.model.entity.User;

import java.util.List;

/**
 * 认证与权限服务
 *
 * @author blog
 */
public interface AuthService {

    User getUserById(Long userId);

    User login(String username, String password);

    List<String> getPermissionCodes(Long userId);

    List<com.blog.model.entity.Permission> getMenuTree(Long userId);

    void logout();
}
