package com.blog.controller.portal;

import com.blog.config.BlogProperties;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 门户-图片代理控制器。
 *
 * <p>由于 MinIO 服务端口可能未对外暴露，客户端无法直接访问 MinIO 存储的图片。
 * 此控制器提供代理访问，将 /portal/image/{prefix}/{filename} 请求转发到 MinIO。
 *
 * @author 一朝风月
 */
@Slf4j
@RestController
@RequestMapping("/portal/image")
@RequiredArgsConstructor
public class PortalImageController {

    private final BlogProperties blogProperties;

    private static final Map<String, String> CONTENT_TYPE_MAP = new HashMap<>();

    static {
        CONTENT_TYPE_MAP.put(".jpg", "image/jpeg");
        CONTENT_TYPE_MAP.put(".jpeg", "image/jpeg");
        CONTENT_TYPE_MAP.put(".png", "image/png");
        CONTENT_TYPE_MAP.put(".gif", "image/gif");
        CONTENT_TYPE_MAP.put(".webp", "image/webp");
        CONTENT_TYPE_MAP.put(".bmp", "image/bmp");
    }

    /**
     * 通过代理获取 MinIO 中的图片。
     *
     * @param prefix 文件前缀（如 avatar、article）
     * @param filename 文件名
     * @return 图片响应
     */
    @GetMapping("/{prefix}/{filename}")
    public ResponseEntity<byte[]> getImage(
            @PathVariable("prefix") String prefix,
            @PathVariable("filename") String filename) {
        try {
            BlogProperties.MinioProperties m = blogProperties.getMinio();
            if (m.getEndpoint() == null || m.getAccessKey() == null || 
                m.getSecretKey() == null || m.getBucket() == null) {
                log.warn("MinIO 未配置");
                return ResponseEntity.notFound().build();
            }

            MinioClient client = MinioClient.builder()
                    .endpoint(m.getEndpoint())
                    .credentials(m.getAccessKey(), m.getSecretKey())
                    .build();

            String objectName = prefix + "/" + filename;
            try (InputStream in = client.getObject(
                    GetObjectArgs.builder()
                            .bucket(m.getBucket())
                            .object(objectName)
                            .build())) {
                byte[] data = in.readAllBytes();
                String contentType = getContentType(filename);
                
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                        .header(HttpHeaders.CACHE_CONTROL, "max-age=31536000")
                        .body(data);
            }
        } catch (Exception e) {
            log.error("获取图片失败: {}/{}", prefix, filename, e);
            return ResponseEntity.notFound().build();
        }
    }

    private String getContentType(String filename) {
        if (filename == null) {
            return "application/octet-stream";
        }
        int idx = filename.lastIndexOf('.');
        if (idx >= 0) {
            String ext = filename.substring(idx).toLowerCase();
            String type = CONTENT_TYPE_MAP.get(ext);
            if (type != null) {
                return type;
            }
        }
        return "application/octet-stream";
    }
}
