package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户 Mapper
 *
 * @author blog
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户ID查询角色编码列表
     */
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询权限编码列表
     */
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);
}
