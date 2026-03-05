package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.model.entity.Role;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色 Mapper
 *
 * @author blog
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {
}
