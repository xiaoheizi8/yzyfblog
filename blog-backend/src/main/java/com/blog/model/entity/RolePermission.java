package com.blog.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色-权限关联
 *
 * @author blog
 */
@Data
@TableName("sys_role_permission")
public class RolePermission extends BaseEntity {

    @TableId
    private Long id;
    private Long roleId;
    private Long permissionId;
}
