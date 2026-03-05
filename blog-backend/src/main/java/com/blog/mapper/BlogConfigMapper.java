package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.model.entity.BlogConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 博客配置 Mapper
 *
 * @author blog
 */
@Mapper
public interface BlogConfigMapper extends BaseMapper<BlogConfig> {
}
