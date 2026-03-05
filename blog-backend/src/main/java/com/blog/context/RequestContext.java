package com.blog.context;

import lombok.Data;

/**
 * 请求上下文。
 *
 * <p>通过 ThreadLocal 存储当前请求的用户ID、IP、请求ID 等信息，便于在服务层与异步任务中使用。
 *
 * @author 一朝风月
 */
@Data
public class RequestContext {

    private Long userId;
    private String ip;
    private String requestId;
}

