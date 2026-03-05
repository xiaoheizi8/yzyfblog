package com.blog.context;

/**
 * 请求上下文持有者。
 *
 * <p>基于 ThreadLocal 存取 {@link RequestContext}，在拦截器与业务代码之间传递。
 *
 * @author 一朝风月
 */
public class RequestContextHolder {

    private static final ThreadLocal<RequestContext> CONTEXT = new ThreadLocal<>();

    public static void set(RequestContext ctx) {
        CONTEXT.set(ctx);
    }

    public static RequestContext get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}

