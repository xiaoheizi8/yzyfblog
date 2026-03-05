package com.blog.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 额外的 WebMvc 配置。
 *
 * <p>注册请求上下文拦截器等，与权限拦截区分开。
 *
 * @author 一朝风月
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcExtraConfig implements WebMvcConfigurer {

    private final RequestContextInterceptor requestContextInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestContextInterceptor)
                .addPathPatterns("/**");
    }
}

