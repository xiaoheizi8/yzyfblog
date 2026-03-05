package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.model.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限/菜单 Mapper
 *
 * @author blog
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据角色ID列表查询菜单树（用于前端动态菜单）
     */
    List<Permission> selectMenuTreeByRoleIds(@Param("roleIds") List<Long> roleIds);
}
