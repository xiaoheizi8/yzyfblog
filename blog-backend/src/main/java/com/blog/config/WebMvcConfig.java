package com.blog.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web MVC 静态资源映射配置。
 *
 * <p>将 /uploads/** 映射到磁盘上的上传目录（例如 D:/blog/uploads），
 * 这样数据库中只需要保存 URL（/uploads/xxx.jpg），浏览器即可直接访问。
 *
 * @author 一朝风月
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final BlogProperties blogProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将配置的 uploadPath 转为绝对路径，例如 D:/blog/uploads
        String root = Paths.get(blogProperties.getUploadPath())
                .toAbsolutePath()
                .toString()
                .replace("\\", "/");
        if (!root.endsWith("/")) {
            root = root + "/";
        }
        // 映射 /uploads/** 到本地磁盘目录
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + root);
    }
}

