package com.blog.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置：路由鉴权
 *
 * @author blog
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/admin/**")
                .excludePathPatterns(
                        "/admin/auth/login",
                        "/admin/auth/oauth/**",
                        "/portal/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                );
    }
}
