package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.model.entity.ArticleTip;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章打赏记录 Mapper。
 *
 * @author 一朝风月
 */
@Mapper
public interface ArticleTipMapper extends BaseMapper<ArticleTip> {
}

