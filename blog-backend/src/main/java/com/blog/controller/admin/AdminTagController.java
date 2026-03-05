package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.annotation.Log;
import com.blog.common.Result;
import com.blog.model.entity.Tag;
import com.blog.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理端-标签管理。
 *
 * <p>用于文章发布/编辑时选择与维护标签。
 *
 * @author 一朝风月
 */
@RestController
@RequestMapping("/admin/tag")
@RequiredArgsConstructor
public class AdminTagController {

    private final TagMapper tagMapper;

    @GetMapping("/list")
    public Result<List<Tag>> list() {
        List<Tag> list = tagMapper.selectList(
                new LambdaQueryWrapper<Tag>()
                        .orderByDesc(Tag::getCreateTime));
        return Result.ok(list);
    }

    @PostMapping
    @Log(module = "标签", operation = "新增标签")
    public Result<Tag> create(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            return Result.fail("标签名称不能为空");
        }
        Tag exist = tagMapper.selectOne(
                new LambdaQueryWrapper<Tag>()
                        .eq(Tag::getName, name)
                        .last("limit 1"));
        if (exist != null) {
            return Result.ok(exist);
        }
        Tag t = new Tag();
        t.setName(name);
        t.setSlug(name);
        t.setCreateTime(LocalDateTime.now());
        tagMapper.insert(t);
        return Result.ok(t);
    }

    @DeleteMapping("/{id}")
    @Log(module = "标签", operation = "删除标签")
    public Result<Void> delete(@PathVariable Long id) {
        tagMapper.deleteById(id);
        return Result.ok();
    }
}

