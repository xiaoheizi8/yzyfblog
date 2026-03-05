package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.model.entity.User;
import com.blog.model.entity.UserRole;
import com.blog.mapper.UserMapper;
import com.blog.mapper.UserRoleMapper;
import com.blog.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 后台用户管理实现
 *
 * @author blog
 */
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    @Override
    public IPage<User> page(Page<User> page, String username, Integer status) {
        LambdaQueryWrapper<User> q = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            q.like(User::getUsername, username).or().like(User::getNickname, username);
        }
        if (status != null) {
            q.eq(User::getStatus, status);
        }
        q.orderByDesc(User::getCreateTime);
        IPage<User> result = userMapper.selectPage(page, q);
        result.getRecords().forEach(u -> u.setPassword(null));
        return result;
    }

    @Override
    public User getById(Long id) {
        User u = userMapper.selectById(id);
        if (u != null) u.setPassword(null);
        return u;
    }

    @Override
    public void save(User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(ENCODER.encode(user.getPassword()));
        }
        userMapper.insert(user);
    }

    @Override
    public void update(User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(ENCODER.encode(user.getPassword()));
        } else {
            User exist = userMapper.selectById(user.getId());
            if (exist != null) user.setPassword(exist.getPassword());
        }
        userMapper.updateById(user);
    }

    @Override
    public void deleteById(Long id) {
        userMapper.deleteById(id);
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));
    }

    @Override
    public void assignRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                UserRole ur = new UserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
    }

    @Override
    public List<Long> getRoleIdsByUserId(Long userId) {
        return userRoleMapper.selectRoleIdsByUserId(userId);
    }

    @Override
    public void updateStatus(Long userId, Integer status) {
        if (userId == null || status == null) {
            return;
        }
        User u = new User();
        u.setId(userId);
        u.setStatus(status);
        userMapper.updateById(u);
    }
}
