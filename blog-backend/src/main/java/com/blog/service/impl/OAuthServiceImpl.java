package com.blog.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.blog.model.entity.User;
import com.blog.mapper.UserMapper;
import com.blog.service.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 第三方登录实现（示例：Gitee，其他类似）
 *
 * @author blog
 */
@Service
@RequiredArgsConstructor
public class OAuthServiceImpl implements OAuthService {

    private final UserMapper userMapper;

    @Value("${blog.oauth2.gitee.client-id:}")
    private String giteeClientId;
    @Value("${blog.oauth2.gitee.client-secret:}")
    private String giteeClientSecret;

    @Override
    public String getAuthorizeUrl(String source) {
        if ("gitee".equalsIgnoreCase(source)) {
            return "https://gitee.com/oauth/authorize?client_id=" + giteeClientId
                    + "&redirect_uri=" + "http://localhost:8080/api/portal/oauth/callback/gitee"
                    + "&response_type=code&state=" + UUID.randomUUID();
        }
        throw new UnsupportedOperationException("暂不支持: " + source);
    }

    @Override
    public User loginByCode(String source, String code, String state) {
        if ("gitee".equalsIgnoreCase(source)) {
            // 实际应请求 Gitee token + user 接口，再创建或绑定用户
            User user = new User();
            user.setId(1L);
            user.setNickname("Gitee用户");
            user.setOauthSource("gitee");
            user.setOauthId("gitee_" + code);
            StpUtil.login(user.getId());
            user.setPassword(null);
            return user;
        }
        throw new UnsupportedOperationException("暂不支持: " + source);
    }
}
