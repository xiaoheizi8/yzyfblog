package com.blog.controller.portal;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.Result;
import com.blog.model.entity.Tag;
import com.blog.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 门户-标签.
 *
 * <p>提供标签列表，用于文章发布时按标签分类。
 *
 * @author 一朝风月
 */
@RestController
@RequestMapping("/portal/tag")
@RequiredArgsConstructor
public class PortalTagController {

    private final TagMapper tagMapper;

    /**
     * 标签列表（按创建时间倒序）。
     */
    @GetMapping("/list")
    public Result<List<Tag>> list() {
        List<Tag> list = tagMapper.selectList(
                new LambdaQueryWrapper<Tag>()
                        .orderByDesc(Tag::getCreateTime));
        return Result.ok(list);
    }
}

