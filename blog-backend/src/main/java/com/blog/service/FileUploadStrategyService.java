package com.blog.service;

import com.blog.config.BlogProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传策略，根据配置选择本地上传或 MinIO 上传。
 *
 * @author 一朝风月
 */
@Service
public class FileUploadStrategyService {

    private final BlogProperties blogProperties;
    private final FileUploadService localFileUploadService;
    private final FileUploadService minioFileUploadService;

    public FileUploadStrategyService(BlogProperties blogProperties,
                                     @Qualifier("localFileUploadService") FileUploadService localFileUploadService,
                                     @Qualifier("minioFileUploadService") FileUploadService minioFileUploadService) {
        this.blogProperties = blogProperties;
        this.localFileUploadService = localFileUploadService;
        this.minioFileUploadService = minioFileUploadService;
    }

    public String upload(MultipartFile file, String pathPrefix) {
        String mode = blogProperties.getUploadMode();
        if ("minio".equalsIgnoreCase(mode)) {
            return minioFileUploadService.upload(file, pathPrefix);
        }
        return localFileUploadService.upload(file, pathPrefix);
    }
}
