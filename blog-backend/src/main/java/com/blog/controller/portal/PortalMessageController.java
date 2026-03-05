package com.blog.controller.portal;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.Result;
import com.blog.model.entity.Message;
import com.blog.mapper.MessageMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 门户-留言（弹幕墙数据源）
 *
 * @author blog
 */
@RestController
@RequestMapping("/portal/message")
@RequiredArgsConstructor
public class PortalMessageController {

    private final MessageMapper messageMapper;

    @Operation(summary = "留言列表（弹幕墙）")
    @GetMapping("/list")
    public Result<List<Message>> list(@RequestParam(defaultValue = "100") int limit) {
        List<Message> list = messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getStatus, 1)
                        .orderByDesc(Message::getCreateTime)
                        .last("LIMIT " + limit));
        return Result.ok(list);
    }

    @Operation(summary = "提交留言")
    @PostMapping("/submit")
    public Result<Message> submit(@RequestBody Message message) {
        message.setStatus(1);
        messageMapper.insert(message);
        return Result.ok(message);
    }
}
