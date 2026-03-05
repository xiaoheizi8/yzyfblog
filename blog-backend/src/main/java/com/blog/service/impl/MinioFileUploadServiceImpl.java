package com.blog.service.impl;

import com.blog.config.BlogProperties;
import com.blog.service.FileUploadService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

/**
 * MinIO 文件上传实现。
 *
 * <p>根据配置将文件上传到 MinIO，对外返回可直接访问的 URL。
 *
 * @author 一朝风月
 */
@Slf4j
@Service("minioFileUploadService")
@RequiredArgsConstructor
public class MinioFileUploadServiceImpl implements FileUploadService {

    private final BlogProperties blogProperties;

    @Override
    public String upload(MultipartFile file, String pathPrefix) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        BlogProperties.MinioProperties m = blogProperties.getMinio();
        if (m.getEndpoint() == null || m.getAccessKey() == null || m.getSecretKey() == null || m.getBucket() == null) {
            throw new IllegalStateException("MinIO 未配置 endpoint/accessKey/secretKey/bucket");
        }
        String suffix = getSuffix(file.getOriginalFilename());
        String objectName = (pathPrefix != null ? pathPrefix + "/" : "")
                + UUID.randomUUID().toString().replace("-", "") + suffix;
        try (InputStream in = file.getInputStream()) {
            MinioClient client = MinioClient.builder()
                    .endpoint(m.getEndpoint())
                    .credentials(m.getAccessKey(), m.getSecretKey())
                    .build();
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(m.getBucket())
                            .object(objectName)
                            .stream(in, in.available(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            String domain = m.getDomain();
            if (domain == null || domain.isEmpty()) {
                domain = m.getEndpoint();
            }
            if (!domain.endsWith("/")) {
                domain += "/";
            }
            return domain + objectName;
        } catch (Exception e) {
            log.error("MinIO 上传失败", e);
            throw new RuntimeException("上传失败: " + e.getMessage());
        }
    }

    private static String getSuffix(String name) {
        if (name == null || !name.contains(".")) {
            return "";
        }
        return name.substring(name.lastIndexOf('.'));
    }
}

