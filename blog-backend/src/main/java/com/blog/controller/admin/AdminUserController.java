package com.blog.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.annotation.Idempotent;
import com.blog.annotation.Log;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.model.entity.Role;
import com.blog.model.entity.User;
import com.blog.mapper.RoleMapper;
import com.blog.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端-用户管理
 *
 * @author blog
 */
@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final RoleMapper roleMapper;

    @Operation(summary = "分页列表")
    @GetMapping("/page")
    public Result<PageResult<User>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status) {
        Page<User> page = new Page<>(current, size);
        var result = adminUserService.page(page, username, status);
        return Result.ok(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    @Operation(summary = "详情")
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.ok(adminUserService.getById(id));
    }

    @Operation(summary = "新增")
    @PostMapping
    @Log(module = "用户", operation = "新增用户")
    public Result<Void> save(@RequestBody User user) {
        adminUserService.save(user);
        return Result.ok();
    }

    @Operation(summary = "修改")
    @PutMapping
    @Log(module = "用户", operation = "修改用户")
    public Result<Void> update(@RequestBody User user) {
        adminUserService.update(user);
        return Result.ok();
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    @Log(module = "用户", operation = "删除用户")
    public Result<Void> delete(@PathVariable Long id) {
        adminUserService.deleteById(id);
        return Result.ok();
    }

    @Operation(summary = "禁用用户")
    @PutMapping("/{id}/disable")
    @Log(module = "用户", operation = "禁用用户")
    @Idempotent(expireSeconds = 5)
    public Result<Void> disable(@PathVariable Long id) {
        adminUserService.updateStatus(id, 0);
        return Result.ok();
    }

    @Operation(summary = "启用用户")
    @PutMapping("/{id}/enable")
    @Log(module = "用户", operation = "启用用户")
    @Idempotent(expireSeconds = 5)
    public Result<Void> enable(@PathVariable Long id) {
        adminUserService.updateStatus(id, 1);
        return Result.ok();
    }

    @Operation(summary = "分配角色")
    @PutMapping("/{id}/roles")
    @Log(module = "用户", operation = "分配角色")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        adminUserService.assignRoles(id, roleIds);
        return Result.ok();
    }

    @Operation(summary = "用户已选角色ID列表")
    @GetMapping("/{id}/roles")
    public Result<List<Long>> getRoleIds(@PathVariable Long id) {
        return Result.ok(adminUserService.getRoleIdsByUserId(id));
    }

    @Operation(summary = "角色列表（下拉用）")
    @GetMapping("/roles/options")
    public Result<List<Role>> roleOptions() {
        return Result.ok(roleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Role>()
                        .eq(Role::getStatus, 1)
                        .orderByAsc(Role::getSortOrder)));
    }
}
