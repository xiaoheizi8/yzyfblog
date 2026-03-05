package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.model.entity.UserFavorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户收藏 Mapper
 *
 * @author 一朝风月
 */
@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {
}
