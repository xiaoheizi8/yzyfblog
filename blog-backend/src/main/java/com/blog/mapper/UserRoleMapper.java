package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.model.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户-角色 Mapper
 *
 * @author blog
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
}
