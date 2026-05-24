package com.blog.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @className: WxConfigDTO
 * @description: change微信配置
 * @author: 风不止
 * @code: 面向自己, 面向未来
 * @createTime: 2026/3/21 12:59
 */
@Data
public class WxConfigDTO implements Serializable {
    private Integer id;
    private String winterfly;
    private String pwd;

}
