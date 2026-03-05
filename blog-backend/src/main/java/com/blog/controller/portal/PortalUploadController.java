package com.blog.controller.portal;

import com.blog.common.Result;
import com.blog.service.FileUploadStrategyService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 门户-文件上传（文章图片等）。
 *
 * <p>复用统一的文件上传策略（本地 / MinIO），仅对外提供简洁的上传接口。
 *
 * @author 一朝风月
 */
@RestController
@RequestMapping("/portal/upload")
@RequiredArgsConstructor
public class PortalUploadController {

    private final FileUploadStrategyService fileUploadStrategyService;

    @Operation(summary = "上传单张图片（文章图片、头像等）")
    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        String url = fileUploadStrategyService.upload(file, "article");
        return Result.ok(url);
    }

    @Operation(summary = "上传头像图片")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String url = fileUploadStrategyService.upload(file, "avatar");
        return Result.ok(url);
    }

    @Operation(summary = "批量上传图片（最多 9 张）")
    @PostMapping("/images")
    public Result<List<String>> uploadImages(@RequestParam("files") List<MultipartFile> files) {
        java.util.List<String> urls = new java.util.ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            urls.add(fileUploadStrategyService.upload(file, "article"));
        }
        return Result.ok(urls);
    }
}

