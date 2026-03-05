package com.blog.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限/菜单表（RBAC，支持动态菜单）
 *
 * @author blog
 */
@Data
@TableName("sys_permission")
public class Permission extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    private String permissionCode;
    private String permissionName;
    private String permissionType;  // menu / button
    private String path;
    private String component;
    private String icon;
    private Integer sortOrder;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")

    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")

    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
