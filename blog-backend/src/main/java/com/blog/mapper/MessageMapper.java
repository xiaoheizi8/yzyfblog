package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.model.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * 留言 Mapper（弹幕墙）
 *
 * @author blog
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
