package com.blog.config;

import cn.dev33.satoken.stp.StpUtil;
import com.blog.context.RequestContext;
import com.blog.context.RequestContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * 请求上下文拦截器。
 *
 * <p>在每次 HTTP 请求进入时填充 {@link RequestContext}，请求结束后清理。
 *
 * @author 一朝风月
 */
@Slf4j
@Component
public class RequestContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        RequestContext ctx = new RequestContext();
        if (StpUtil.isLogin()) {
            ctx.setUserId(StpUtil.getLoginIdAsLong());
        }
        ctx.setIp(getClientIp(request));
        String requestId = request.getHeader("X-Request-Id");
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString().replace("-", "");
        }
        ctx.setRequestId(requestId);
        RequestContextHolder.set(ctx);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        RequestContextHolder.clear();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            int idx = ip.indexOf(',');
            return idx > 0 ? ip.substring(0, idx).trim() : ip;
        }
        ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

