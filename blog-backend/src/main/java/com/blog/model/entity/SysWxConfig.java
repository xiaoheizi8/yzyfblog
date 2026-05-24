package com.blog.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @className: SysWxConfig
 * @description: 针对于移动端的配置启动
 * @author: 风不止
 * @code: 面向自己, 面向未来
 * @createTime: 2026/3/21 9:11
 */

@Data
@TableName("sys_wx_config")
public class SysWxConfig implements Serializable {
    private Integer id;
    private String winterfly;
    private String pwd;
    private String config;
}
