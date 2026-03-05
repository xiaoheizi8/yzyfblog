package com.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.model.entity.User;

import java.util.List;

/**
 * 后台用户管理
 *
 * @author blog
 */
public interface AdminUserService {

    IPage<User> page(Page<User> page, String username, Integer status);

    User getById(Long id);

    void save(User user);

    void update(User user);

    void deleteById(Long id);

    void assignRoles(Long userId, List<Long> roleIds);

    List<Long> getRoleIdsByUserId(Long userId);

    /**
     * 更新用户状态：1 正常，0 禁用。
     *
     * @param userId 用户ID
     * @param status 状态值
     */
    void updateStatus(Long userId, Integer status);
}
