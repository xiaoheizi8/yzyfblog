package com.blog.service.impl;

import com.blog.config.BlogProperties;
import com.blog.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 本地上传实现。
 *
 * <p>将文件保存到本机磁盘指定目录，并返回相对访问路径。
 *
 * @author 一朝风月
 */
@Slf4j
@Service("localFileUploadService")
@RequiredArgsConstructor
public class LocalFileUploadServiceImpl implements FileUploadService {

    private final BlogProperties blogProperties;

    @Override
    public String upload(MultipartFile file, String pathPrefix) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        // 统一将配置的路径转换为绝对路径，避免相对路径被 Tomcat 解析到临时 work 目录
        Path root = Paths.get(blogProperties.getUploadPath()).toAbsolutePath().normalize();
        String suffix = getSuffix(file.getOriginalFilename());
        String fileName = (pathPrefix != null ? pathPrefix + "/" : "") + UUID.randomUUID().toString().replace("-", "") + suffix;
        Path target = root.resolve(fileName);
        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target.toFile());
        } catch (IOException e) {
            log.error("本地上传失败", e);
            throw new RuntimeException("上传失败: " + e.getMessage());
        }
        return "/uploads/" + fileName.replace("\\", "/");
    }

    private static String getSuffix(String name) {
        if (name == null || !name.contains(".")) {
            return "";
        }
        return name.substring(name.lastIndexOf('.'));
    }
}
