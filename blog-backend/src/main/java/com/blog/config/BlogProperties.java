package com.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 博客业务配置（与 application.yml blog 节点绑定，部分可由 DB 覆盖）
 *
 * <p>支持搜索模式、上传模式（本地 / MinIO）等。
 *
 * @author 一朝风月
 */
@Data
@Component
@ConfigurationProperties(prefix = "blog")
public class BlogProperties {

    /** 搜索模式: mysql | elasticsearch */
    private String searchMode = "mysql";
    /** 上传模式: local | minio */
    private String uploadMode = "local";
    private String uploadPath = "./uploads";
    private MinioProperties minio = new MinioProperties();

    /**
     * MinIO 配置（upload-mode=minio 时生效）
     */
    @Data
    public static class MinioProperties {
        /** MinIO 访问端点，例如 http://127.0.0.1:9000 */
        private String endpoint;
        private String accessKey;
        private String secretKey;
        /** 桶名称 */
        private String bucket;
        /**
         * 对外访问域名（可为 Nginx 反向代理地址），用于拼接文件 URL。
         * 若为空则默认使用 endpoint。
         */
        private String domain;
    }
}
