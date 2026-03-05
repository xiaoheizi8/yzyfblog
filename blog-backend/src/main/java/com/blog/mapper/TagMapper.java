package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.model.entity.Tag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 标签 Mapper
 *
 * @author blog
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {
}
