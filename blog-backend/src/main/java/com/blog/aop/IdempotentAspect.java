package com.blog.aop;

import com.blog.annotation.Idempotent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

/**
 * 幂等性切面。
 *
 * <p>基于请求头 Idempotency-Key + Redis setIfAbsent 实现简单防重放。
 *
 * @author 一朝风月
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class IdempotentAspect {

    private final StringRedisTemplate stringRedisTemplate;

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return joinPoint.proceed();
        }
        HttpServletRequest request = attrs.getRequest();
        String headerName = idempotent.header();
        String key = request.getHeader(headerName);
        if (key == null || key.isBlank()) {
            // 未携带幂等 key，直接放行（兼容旧调用）
            return joinPoint.proceed();
        }
        String redisKey = "idem:" + headerName + ":" + key;
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(redisKey, "1", Duration.ofSeconds(idempotent.expireSeconds()));
        if (Boolean.FALSE.equals(success)) {
            log.warn("检测到重复提交, key={}", redisKey);
            throw new RuntimeException("请不要重复提交");
        }
        return joinPoint.proceed();
    }
}

