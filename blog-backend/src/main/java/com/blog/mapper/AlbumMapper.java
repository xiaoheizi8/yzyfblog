package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.model.entity.Album;
import org.apache.ibatis.annotations.Mapper;

/**
 * 相册 Mapper
 *
 * @author blog
 */
@Mapper
public interface AlbumMapper extends BaseMapper<Album> {
}
