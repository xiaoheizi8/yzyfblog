package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.model.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper（AOP 写入）
 *
 * @author blog
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
