package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.model.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分类 Mapper
 *
 * @author blog
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
