package com.blog.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传统一接口（本地上传 / MinIO 可配置切换）。
 *
 * @author 一朝风月
 */
public interface FileUploadService {

    /**
     * 上传文件，返回访问 URL
     */
    String upload(MultipartFile file, String pathPrefix);
}
