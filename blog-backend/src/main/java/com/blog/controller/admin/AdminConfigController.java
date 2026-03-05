package com.blog.controller.admin;

import com.blog.annotation.Log;
import com.blog.common.Result;
import com.blog.model.entity.BlogConfig;
import com.blog.mapper.BlogConfigMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 管理端-博客配置（背景图、站点信息等）
 *
 * @author blog
 */
@RestController
@RequestMapping("/admin/config")
@RequiredArgsConstructor
public class AdminConfigController {

    private final BlogConfigMapper blogConfigMapper;
    private final com.blog.service.FileUploadStrategyService fileUploadStrategyService;

    @Operation(summary = "获取所有配置")
    @GetMapping("/list")
    public Result<List<BlogConfig>> list() {
        return Result.ok(blogConfigMapper.selectList(null));
    }

    @Operation(summary = "批量更新配置")
    @PutMapping("/batch")
    @Log(module = "配置", operation = "批量更新配置")
    public Result<Void> batchUpdate(@RequestBody Map<String, String> configs) {
        for (Map.Entry<String, String> e : configs.entrySet()) {
            BlogConfig c = blogConfigMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BlogConfig>()
                            .eq(BlogConfig::getConfigKey, e.getKey()));
            if (c != null) {
                c.setConfigValue(e.getValue());
                blogConfigMapper.updateById(c);
            } else {
                c = new BlogConfig();
                c.setConfigKey(e.getKey());
                c.setConfigValue(e.getValue());
                blogConfigMapper.insert(c);
            }
        }
        return Result.ok();
    }

    @Operation(summary = "上传背景图")
    @PostMapping("/uploadBackground")
    @Log(module = "配置", operation = "上传背景图")
    public Result<String> uploadBackground(@RequestParam("file") MultipartFile file) {
        String url = fileUploadStrategyService.upload(file, "background");
        return Result.ok(url);
    }
}
