package com.blog.service;

import com.blog.model.dto.WxConfigDTO;
import com.blog.model.entity.SysWxConfig;

/**
 * @className: SysWxConfigService
 * @description: uniapp端实现微信小程序端配置启动
 * @author: 风不止
 * @code: 面向自己, 面向未来
 * @createTime: 2026/3/21 9:16
 */
public interface SysWxConfigService {
    SysWxConfig getWxConfig(String winterfly);
    SysWxConfig getWxConfig();
    Integer updateConfigInteger(WxConfigDTO sysWxConfig);
}
