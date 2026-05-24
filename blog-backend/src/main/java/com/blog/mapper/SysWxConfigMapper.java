package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.model.entity.SysWxConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @className: SysWxConfigMapper
 * @description: 移动端是否开启微信小程序
 * @author: 风不止
 * @code: 面向自己, 面向未来
 * @createTime: 2026/3/21 9:13
 */
@Mapper
public interface SysWxConfigMapper  extends BaseMapper<SysWxConfig> {


    @Select("select  id,winterfly,pwd,config from sys_wx_config  where winterfly=#{winterfly}")
    SysWxConfig getWxConfig(@Param("winterfly") String winterfly);


    @Update("update sys_wx_config set winterfly=#{winterfly},pwd=#{pwd} where id=#{id}")
    Integer updateConfigInteger(@Param("winterfly")String winterfly,@Param("pwd") String pwd,@Param("id") Integer id);


}
