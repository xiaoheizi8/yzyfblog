package com.blog.service;

import com.blog.model.entity.User;

/**
 * 第三方登录（Gitee/GitHub/QQ）
 *
 * @author blog
 */
public interface OAuthService {

    String getAuthorizeUrl(String source);

    User loginByCode(String source, String code, String state);
}
